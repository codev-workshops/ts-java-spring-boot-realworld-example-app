package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_generated_id_and_timestamp() {
    Comment comment = new Comment("body", "user1", "article1");
    assertNotNull(comment.getId());
    assertNotNull(comment.getCreatedAt());
    assertEquals("body", comment.getBody());
    assertEquals("user1", comment.getUserId());
    assertEquals("article1", comment.getArticleId());
  }
}
