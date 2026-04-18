package io.spring.infrastructure.external;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentGatewayClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public PaymentGatewayClient(
      RestTemplate restTemplate,
      @Value("${external.payment-gateway.url:http://localhost:9090}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public PaymentResult chargeForPremiumArticle(String userId, double amount) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> body =
        Map.of("userId", userId, "amount", amount, "currency", "USD", "description", "Premium article publishing fee");

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    @SuppressWarnings("unchecked")
    Map<String, Object> response =
        restTemplate.postForObject(baseUrl + "/api/payments/charge", request, Map.class);

    if (response == null) {
      return new PaymentResult(false, null, "No response from payment gateway");
    }

    return new PaymentResult(
        "approved".equals(response.get("status")),
        (String) response.get("transactionId"),
        (String) response.get("message"));
  }

  public static class PaymentResult {
    private final boolean success;
    private final String transactionId;
    private final String message;

    public PaymentResult(boolean success, String transactionId, String message) {
      this.success = success;
      this.transactionId = transactionId;
      this.message = message;
    }

    public boolean isSuccess() {
      return success;
    }

    public String getTransactionId() {
      return transactionId;
    }

    public String getMessage() {
      return message;
    }
  }
}
