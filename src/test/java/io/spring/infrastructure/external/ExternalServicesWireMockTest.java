package io.spring.infrastructure.external;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests demonstrating WireMock for testing external service integrations.
 *
 * <p>This test class simulates three external services that the application cannot connect to in a
 * test environment:
 *
 * <ul>
 *   <li><b>Payment Gateway</b> (port 9090) - Charges for premium article publishing
 *   <li><b>Email/SMTP Service</b> (port 9091) - Sends article published notifications
 *   <li><b>Loyalty Service</b> (port 9092) - Awards points when articles are published
 * </ul>
 *
 * <p>WireMock intercepts HTTP calls to these services and returns predefined responses, allowing us
 * to test correctness without real external dependencies.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExternalServicesWireMockTest {

  private static WireMockServer paymentGatewayMock;
  private static WireMockServer emailServiceMock;
  private static WireMockServer loyaltyServiceMock;

  private PaymentGatewayClient paymentGatewayClient;
  private EmailNotificationClient emailNotificationClient;
  private LoyaltyServiceClient loyaltyServiceClient;
  private ArticlePublishingOrchestrator orchestrator;

  @BeforeAll
  static void startWireMockServers() {
    paymentGatewayMock =
        new WireMockServer(WireMockConfiguration.wireMockConfig().port(9090));
    emailServiceMock =
        new WireMockServer(WireMockConfiguration.wireMockConfig().port(9091));
    loyaltyServiceMock =
        new WireMockServer(WireMockConfiguration.wireMockConfig().port(9092));

    paymentGatewayMock.start();
    emailServiceMock.start();
    loyaltyServiceMock.start();
  }

  @AfterAll
  static void stopWireMockServers() {
    paymentGatewayMock.stop();
    emailServiceMock.stop();
    loyaltyServiceMock.stop();
  }

  @BeforeEach
  void setUp() {
    paymentGatewayMock.resetAll();
    emailServiceMock.resetAll();
    loyaltyServiceMock.resetAll();

    RestTemplate restTemplate = new RestTemplate();
    paymentGatewayClient = new PaymentGatewayClient(restTemplate, "http://localhost:9090");
    emailNotificationClient = new EmailNotificationClient(restTemplate, "http://localhost:9091");
    loyaltyServiceClient = new LoyaltyServiceClient(restTemplate, "http://localhost:9092");
    orchestrator =
        new ArticlePublishingOrchestrator(
            paymentGatewayClient, emailNotificationClient, loyaltyServiceClient);
  }

  // =========================================================================
  // Test 1: Payment Gateway — Happy Path
  // =========================================================================
  @Test
  @Order(1)
  @DisplayName("Payment Gateway: should process payment successfully")
  void paymentGateway_happyPath() {
    // Arrange: stub the payment gateway to approve the charge
    paymentGatewayMock.stubFor(
        post(urlEqualTo("/api/payments/charge"))
            .withRequestBody(matchingJsonPath("$.userId", equalTo("user-1")))
            .withRequestBody(matchingJsonPath("$.amount", equalTo("9.99")))
            .withRequestBody(matchingJsonPath("$.currency", equalTo("USD")))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{"
                            + "\"status\": \"approved\","
                            + "\"transactionId\": \"txn-abc-123\","
                            + "\"message\": \"Payment processed successfully\""
                            + "}")));

    // Act
    PaymentGatewayClient.PaymentResult result =
        paymentGatewayClient.chargeForPremiumArticle("user-1", 9.99);

    // Assert
    assertTrue(result.isSuccess(), "Payment should be approved");
    assertEquals("txn-abc-123", result.getTransactionId());
    assertEquals("Payment processed successfully", result.getMessage());

    // Verify the payment gateway received exactly 1 request
    paymentGatewayMock.verify(1, postRequestedFor(urlEqualTo("/api/payments/charge")));
  }

  // =========================================================================
  // Test 2: Payment Gateway — Declined Card
  // =========================================================================
  @Test
  @Order(2)
  @DisplayName("Payment Gateway: should handle declined payment")
  void paymentGateway_declined() {
    paymentGatewayMock.stubFor(
        post(urlEqualTo("/api/payments/charge"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{"
                            + "\"status\": \"declined\","
                            + "\"transactionId\": null,"
                            + "\"message\": \"Insufficient funds\""
                            + "}")));

    PaymentGatewayClient.PaymentResult result =
        paymentGatewayClient.chargeForPremiumArticle("user-1", 9.99);

    assertFalse(result.isSuccess(), "Payment should be declined");
    assertEquals("Insufficient funds", result.getMessage());
  }

  // =========================================================================
  // Test 3: Email Notification — Happy Path
  // =========================================================================
  @Test
  @Order(3)
  @DisplayName("Email Service: should send article published notification")
  void emailService_happyPath() {
    emailServiceMock.stubFor(
        post(urlEqualTo("/api/notifications/email"))
            .withRequestBody(matchingJsonPath("$.to", equalTo("john@example.com")))
            .withRequestBody(matchingJsonPath("$.template", equalTo("article-published")))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{"
                            + "\"status\": \"sent\","
                            + "\"messageId\": \"msg-email-456\","
                            + "\"message\": \"Email delivered to john@example.com\""
                            + "}")));

    EmailNotificationClient.EmailResult result =
        emailNotificationClient.sendArticlePublishedNotification(
            "john@example.com", "My Spring Boot Article", "my-spring-boot-article");

    assertTrue(result.isSent(), "Email should be sent");
    assertEquals("msg-email-456", result.getMessageId());

    // Verify request body contains correct subject
    emailServiceMock.verify(
        postRequestedFor(urlEqualTo("/api/notifications/email"))
            .withRequestBody(
                matchingJsonPath(
                    "$.subject",
                    containing("My Spring Boot Article"))));
  }

  // =========================================================================
  // Test 4: Email Service — SMTP Server Down (simulated 503)
  // =========================================================================
  @Test
  @Order(4)
  @DisplayName("Email Service: should handle SMTP server unavailable")
  void emailService_serverDown() {
    emailServiceMock.stubFor(
        post(urlEqualTo("/api/notifications/email"))
            .willReturn(
                aResponse()
                    .withStatus(503)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\": \"SMTP server unavailable\"}")));

    // The RestTemplate will throw an exception for 5xx status
    assertThrows(
        Exception.class,
        () ->
            emailNotificationClient.sendArticlePublishedNotification(
                "john@example.com", "Test Article", "test-article"),
        "Should throw exception when email service is down");
  }

  // =========================================================================
  // Test 5: Loyalty Service — Happy Path
  // =========================================================================
  @Test
  @Order(5)
  @DisplayName("Loyalty Service: should award points for article publication")
  void loyaltyService_happyPath() {
    loyaltyServiceMock.stubFor(
        post(urlEqualTo("/api/loyalty/award"))
            .withRequestBody(matchingJsonPath("$.userId", equalTo("user-1")))
            .withRequestBody(matchingJsonPath("$.action", equalTo("ARTICLE_PUBLISHED")))
            .withRequestBody(matchingJsonPath("$.points", equalTo("50")))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{"
                            + "\"status\": \"awarded\","
                            + "\"pointsAwarded\": 50,"
                            + "\"totalPoints\": 350,"
                            + "\"message\": \"Points awarded for ARTICLE_PUBLISHED\""
                            + "}")));

    LoyaltyServiceClient.LoyaltyResult result =
        loyaltyServiceClient.awardPointsForArticle("user-1", "article-99");

    assertTrue(result.isSuccess(), "Points should be awarded");
    assertEquals(50, result.getPointsAwarded());
    assertEquals(350, result.getTotalPoints());

    loyaltyServiceMock.verify(1, postRequestedFor(urlEqualTo("/api/loyalty/award")));
  }

  // =========================================================================
  // Test 6: Loyalty Service — Slow Response (timeout simulation)
  // =========================================================================
  @Test
  @Order(6)
  @DisplayName("Loyalty Service: should handle slow response gracefully")
  void loyaltyService_slowResponse() {
    loyaltyServiceMock.stubFor(
        post(urlEqualTo("/api/loyalty/award"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withFixedDelay(100) // 100ms delay to simulate slow service
                    .withBody(
                        "{"
                            + "\"status\": \"awarded\","
                            + "\"pointsAwarded\": 50,"
                            + "\"totalPoints\": 400,"
                            + "\"message\": \"Points awarded (delayed)\""
                            + "}")));

    LoyaltyServiceClient.LoyaltyResult result =
        loyaltyServiceClient.awardPointsForArticle("user-1", "article-100");

    assertTrue(result.isSuccess(), "Should still succeed despite delay");
    assertEquals(50, result.getPointsAwarded());
  }

  // =========================================================================
  // Test 7: Full Orchestration — All Services Succeed
  // =========================================================================
  @Test
  @Order(7)
  @DisplayName("Orchestrator: all external services succeed during article publish")
  void orchestrator_allServicesSucceed() {
    // Stub all three services
    stubPaymentSuccess();
    stubEmailSuccess();
    stubLoyaltySuccess();

    // Create test article and user
    Article article =
        new Article(
            "WireMock Demo Article",
            "Testing external services",
            "Article body content",
            Arrays.asList("java", "testing"),
            "user-1");
    User author = new User("john@example.com", "johndoe", "password123", "Bio", "image.jpg");

    // Act
    ArticlePublishingOrchestrator.PublishingResult result =
        orchestrator.processArticlePublished(article, author);

    // Assert all services responded correctly
    assertTrue(result.isPaymentSuccess(), "Payment should succeed");
    assertEquals("txn-orch-001", result.getTransactionId());
    assertTrue(result.isEmailSent(), "Email should be sent");
    assertEquals("msg-orch-001", result.getEmailMessageId());
    assertTrue(result.isLoyaltyPointsAwarded(), "Loyalty points should be awarded");
    assertEquals(50, result.getPointsAwarded());
    assertEquals(500, result.getTotalPoints());
    assertTrue(result.isFullySuccessful(), "Overall result should be fully successful");

    // Verify all three services were called exactly once
    paymentGatewayMock.verify(1, postRequestedFor(urlEqualTo("/api/payments/charge")));
    emailServiceMock.verify(1, postRequestedFor(urlEqualTo("/api/notifications/email")));
    loyaltyServiceMock.verify(1, postRequestedFor(urlEqualTo("/api/loyalty/award")));
  }

  // =========================================================================
  // Test 8: Partial Failure — Payment fails, others succeed
  // =========================================================================
  @Test
  @Order(8)
  @DisplayName("Orchestrator: handles partial failure (payment declined, others succeed)")
  void orchestrator_paymentFails_othersContinue() {
    // Payment gateway returns declined
    paymentGatewayMock.stubFor(
        post(urlEqualTo("/api/payments/charge"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{"
                            + "\"status\": \"declined\","
                            + "\"transactionId\": null,"
                            + "\"message\": \"Card expired\""
                            + "}")));
    stubEmailSuccess();
    stubLoyaltySuccess();

    Article article =
        new Article(
            "Partial Failure Test",
            "Desc",
            "Body",
            Arrays.asList("test"),
            "user-1");
    User author = new User("john@example.com", "johndoe", "password123", "", "");

    ArticlePublishingOrchestrator.PublishingResult result =
        orchestrator.processArticlePublished(article, author);

    // Payment failed but email and loyalty still succeeded
    assertFalse(result.isPaymentSuccess(), "Payment should fail");
    assertTrue(result.isEmailSent(), "Email should still be sent");
    assertTrue(result.isLoyaltyPointsAwarded(), "Loyalty should still work");
    assertFalse(result.isFullySuccessful(), "Overall should NOT be fully successful");

    // All three services were still called
    paymentGatewayMock.verify(1, postRequestedFor(urlEqualTo("/api/payments/charge")));
    emailServiceMock.verify(1, postRequestedFor(urlEqualTo("/api/notifications/email")));
    loyaltyServiceMock.verify(1, postRequestedFor(urlEqualTo("/api/loyalty/award")));
  }

  // =========================================================================
  // Test 9: Total External Failure — All services down
  // =========================================================================
  @Test
  @Order(9)
  @DisplayName("Orchestrator: handles total external failure gracefully")
  void orchestrator_allServicesFail() {
    // All services return 500
    paymentGatewayMock.stubFor(
        post(urlEqualTo("/api/payments/charge"))
            .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));
    emailServiceMock.stubFor(
        post(urlEqualTo("/api/notifications/email"))
            .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));
    loyaltyServiceMock.stubFor(
        post(urlEqualTo("/api/loyalty/award"))
            .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

    Article article =
        new Article(
            "All Fail Test", "Desc", "Body", Arrays.asList("test"), "user-1");
    User author = new User("john@example.com", "johndoe", "password123", "", "");

    // The orchestrator should NOT throw — it catches errors and returns a result
    ArticlePublishingOrchestrator.PublishingResult result =
        orchestrator.processArticlePublished(article, author);

    assertFalse(result.isPaymentSuccess());
    assertFalse(result.isEmailSent());
    assertFalse(result.isLoyaltyPointsAwarded());
    assertFalse(result.isFullySuccessful());
  }

  // =========================================================================
  // Test 10: Verify Request Bodies — Ensure correct data sent to external APIs
  // =========================================================================
  @Test
  @Order(10)
  @DisplayName("Orchestrator: sends correct request payloads to all external services")
  void orchestrator_verifiesCorrectRequestPayloads() {
    stubPaymentSuccess();
    stubEmailSuccess();
    stubLoyaltySuccess();

    Article article =
        new Article(
            "Payload Verification Test",
            "Testing payloads",
            "Body content",
            Arrays.asList("wiremock"),
            "user-42");
    User author = new User("jane@example.com", "janedoe", "pass123", "My bio", "avatar.png");

    // Capture the auto-generated IDs for verification
    String authorId = author.getId();
    String articleId = article.getId();

    orchestrator.processArticlePublished(article, author);

    // Verify payment request contains correct user ID and amount
    paymentGatewayMock.verify(
        postRequestedFor(urlEqualTo("/api/payments/charge"))
            .withRequestBody(matchingJsonPath("$.userId", equalTo(authorId)))
            .withRequestBody(matchingJsonPath("$.amount", equalTo("9.99")))
            .withRequestBody(matchingJsonPath("$.currency", equalTo("USD"))));

    // Verify email request contains correct recipient and article info
    emailServiceMock.verify(
        postRequestedFor(urlEqualTo("/api/notifications/email"))
            .withRequestBody(matchingJsonPath("$.to", equalTo("jane@example.com")))
            .withRequestBody(
                matchingJsonPath("$.subject", containing("Payload Verification Test")))
            .withRequestBody(matchingJsonPath("$.template", equalTo("article-published"))));

    // Verify loyalty request contains correct user and action
    loyaltyServiceMock.verify(
        postRequestedFor(urlEqualTo("/api/loyalty/award"))
            .withRequestBody(matchingJsonPath("$.userId", equalTo(authorId)))
            .withRequestBody(matchingJsonPath("$.action", equalTo("ARTICLE_PUBLISHED")))
            .withRequestBody(matchingJsonPath("$.points", equalTo("50"))));
  }

  // =========================================================================
  // Helper methods to set up common stubs
  // =========================================================================

  private void stubPaymentSuccess() {
    paymentGatewayMock.stubFor(
        post(urlEqualTo("/api/payments/charge"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{"
                            + "\"status\": \"approved\","
                            + "\"transactionId\": \"txn-orch-001\","
                            + "\"message\": \"Payment approved\""
                            + "}")));
  }

  private void stubEmailSuccess() {
    emailServiceMock.stubFor(
        post(urlEqualTo("/api/notifications/email"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{"
                            + "\"status\": \"sent\","
                            + "\"messageId\": \"msg-orch-001\","
                            + "\"message\": \"Email sent successfully\""
                            + "}")));
  }

  private void stubLoyaltySuccess() {
    loyaltyServiceMock.stubFor(
        post(urlEqualTo("/api/loyalty/award"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{"
                            + "\"status\": \"awarded\","
                            + "\"pointsAwarded\": 50,"
                            + "\"totalPoints\": 500,"
                            + "\"message\": \"Points awarded\""
                            + "}")));
  }
}
