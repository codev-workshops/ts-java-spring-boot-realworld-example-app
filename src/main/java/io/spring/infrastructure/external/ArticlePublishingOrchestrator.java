package io.spring.infrastructure.external;

import io.spring.core.article.Article;
import io.spring.core.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrates external service calls when an article is published. This simulates a real-world
 * scenario where publishing an article triggers: 1. Payment processing (premium article fee) 2.
 * Email notification (author notification) 3. Loyalty points award
 */
@Service
public class ArticlePublishingOrchestrator {

  private static final Logger logger = LoggerFactory.getLogger(ArticlePublishingOrchestrator.class);

  private final PaymentGatewayClient paymentGatewayClient;
  private final EmailNotificationClient emailNotificationClient;
  private final LoyaltyServiceClient loyaltyServiceClient;

  public ArticlePublishingOrchestrator(
      PaymentGatewayClient paymentGatewayClient,
      EmailNotificationClient emailNotificationClient,
      LoyaltyServiceClient loyaltyServiceClient) {
    this.paymentGatewayClient = paymentGatewayClient;
    this.emailNotificationClient = emailNotificationClient;
    this.loyaltyServiceClient = loyaltyServiceClient;
  }

  public PublishingResult processArticlePublished(Article article, User author) {
    PublishingResult result = new PublishingResult();

    // Step 1: Charge for premium article
    try {
      PaymentGatewayClient.PaymentResult paymentResult =
          paymentGatewayClient.chargeForPremiumArticle(author.getId(), 9.99);
      result.setPaymentSuccess(paymentResult.isSuccess());
      result.setTransactionId(paymentResult.getTransactionId());
      logger.info(
          "Payment processed for article '{}': success={}, txn={}",
          article.getTitle(),
          paymentResult.isSuccess(),
          paymentResult.getTransactionId());
    } catch (Exception e) {
      logger.error("Payment failed for article '{}': {}", article.getTitle(), e.getMessage());
      result.setPaymentSuccess(false);
    }

    // Step 2: Send email notification
    try {
      EmailNotificationClient.EmailResult emailResult =
          emailNotificationClient.sendArticlePublishedNotification(
              author.getEmail(), article.getTitle(), article.getSlug());
      result.setEmailSent(emailResult.isSent());
      result.setEmailMessageId(emailResult.getMessageId());
      logger.info(
          "Email notification for article '{}': sent={}, msgId={}",
          article.getTitle(),
          emailResult.isSent(),
          emailResult.getMessageId());
    } catch (Exception e) {
      logger.error(
          "Email notification failed for article '{}': {}", article.getTitle(), e.getMessage());
      result.setEmailSent(false);
    }

    // Step 3: Award loyalty points
    try {
      LoyaltyServiceClient.LoyaltyResult loyaltyResult =
          loyaltyServiceClient.awardPointsForArticle(author.getId(), article.getId());
      result.setLoyaltyPointsAwarded(loyaltyResult.isSuccess());
      result.setPointsAwarded(loyaltyResult.getPointsAwarded());
      result.setTotalPoints(loyaltyResult.getTotalPoints());
      logger.info(
          "Loyalty points for article '{}': awarded={}, points={}",
          article.getTitle(),
          loyaltyResult.getPointsAwarded(),
          loyaltyResult.getTotalPoints());
    } catch (Exception e) {
      logger.error(
          "Loyalty service failed for article '{}': {}", article.getTitle(), e.getMessage());
      result.setLoyaltyPointsAwarded(false);
    }

    return result;
  }

  public static class PublishingResult {
    private boolean paymentSuccess;
    private String transactionId;
    private boolean emailSent;
    private String emailMessageId;
    private boolean loyaltyPointsAwarded;
    private int pointsAwarded;
    private int totalPoints;

    public boolean isPaymentSuccess() {
      return paymentSuccess;
    }

    public void setPaymentSuccess(boolean paymentSuccess) {
      this.paymentSuccess = paymentSuccess;
    }

    public String getTransactionId() {
      return transactionId;
    }

    public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
    }

    public boolean isEmailSent() {
      return emailSent;
    }

    public void setEmailSent(boolean emailSent) {
      this.emailSent = emailSent;
    }

    public String getEmailMessageId() {
      return emailMessageId;
    }

    public void setEmailMessageId(String emailMessageId) {
      this.emailMessageId = emailMessageId;
    }

    public boolean isLoyaltyPointsAwarded() {
      return loyaltyPointsAwarded;
    }

    public void setLoyaltyPointsAwarded(boolean loyaltyPointsAwarded) {
      this.loyaltyPointsAwarded = loyaltyPointsAwarded;
    }

    public int getPointsAwarded() {
      return pointsAwarded;
    }

    public void setPointsAwarded(int pointsAwarded) {
      this.pointsAwarded = pointsAwarded;
    }

    public int getTotalPoints() {
      return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
      this.totalPoints = totalPoints;
    }

    public boolean isFullySuccessful() {
      return paymentSuccess && emailSent && loyaltyPointsAwarded;
    }
  }
}
