package io.spring.infrastructure.external;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LoyaltyServiceClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public LoyaltyServiceClient(
      RestTemplate restTemplate,
      @Value("${external.loyalty-service.url:http://localhost:9092}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public LoyaltyResult awardPointsForArticle(String userId, String articleId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> body =
        Map.of(
            "userId", userId,
            "action", "ARTICLE_PUBLISHED",
            "referenceId", articleId,
            "points", 50);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    @SuppressWarnings("unchecked")
    Map<String, Object> response =
        restTemplate.postForObject(baseUrl + "/api/loyalty/award", request, Map.class);

    if (response == null) {
      return new LoyaltyResult(false, 0, 0, "No response from loyalty service");
    }

    return new LoyaltyResult(
        "awarded".equals(response.get("status")),
        ((Number) response.get("pointsAwarded")).intValue(),
        ((Number) response.get("totalPoints")).intValue(),
        (String) response.get("message"));
  }

  public static class LoyaltyResult {
    private final boolean success;
    private final int pointsAwarded;
    private final int totalPoints;
    private final String message;

    public LoyaltyResult(boolean success, int pointsAwarded, int totalPoints, String message) {
      this.success = success;
      this.pointsAwarded = pointsAwarded;
      this.totalPoints = totalPoints;
      this.message = message;
    }

    public boolean isSuccess() {
      return success;
    }

    public int getPointsAwarded() {
      return pointsAwarded;
    }

    public int getTotalPoints() {
      return totalPoints;
    }

    public String getMessage() {
      return message;
    }
  }
}
