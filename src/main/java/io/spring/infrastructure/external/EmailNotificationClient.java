package io.spring.infrastructure.external;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EmailNotificationClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public EmailNotificationClient(
      RestTemplate restTemplate,
      @Value("${external.email-service.url:http://localhost:9091}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public EmailResult sendArticlePublishedNotification(
      String authorEmail, String articleTitle, String articleSlug) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> body =
        Map.of(
            "to", authorEmail,
            "subject", "Your article has been published: " + articleTitle,
            "template", "article-published",
            "templateData", Map.of("title", articleTitle, "slug", articleSlug));

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    @SuppressWarnings("unchecked")
    Map<String, Object> response =
        restTemplate.postForObject(baseUrl + "/api/notifications/email", request, Map.class);

    if (response == null) {
      return new EmailResult(false, null, "No response from email service");
    }

    return new EmailResult(
        "sent".equals(response.get("status")),
        (String) response.get("messageId"),
        (String) response.get("message"));
  }

  public static class EmailResult {
    private final boolean sent;
    private final String messageId;
    private final String message;

    public EmailResult(boolean sent, String messageId, String message) {
      this.sent = sent;
      this.messageId = messageId;
      this.message = message;
    }

    public boolean isSent() {
      return sent;
    }

    public String getMessageId() {
      return messageId;
    }

    public String getMessage() {
      return message;
    }
  }
}
