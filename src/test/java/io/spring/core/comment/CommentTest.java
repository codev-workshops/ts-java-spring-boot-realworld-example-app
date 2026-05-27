package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  void constructor_sets_fields_and_generates_id() {
    Comment comment = new Comment("body text", "user-id", "article-id");

    assertNotNull(comment.getId());
    assertFalse(comment.getId().isEmpty());
    assertEquals("body text", comment.getBody());
    assertEquals("user-id", comment.getUserId());
    assertEquals("article-id", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void constructor_generates_unique_ids() {
    Comment c1 = new Comment("body1", "u1", "a1");
    Comment c2 = new Comment("body2", "u2", "a2");

    assertNotEquals(c1.getId(), c2.getId());
  }
}
