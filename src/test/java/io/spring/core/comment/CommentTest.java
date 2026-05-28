package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  void should_create_comment() {
    Comment comment = new Comment("body text", "user-id", "article-id");
    assertNotNull(comment.getId());
    assertEquals("body text", comment.getBody());
    assertEquals("user-id", comment.getUserId());
    assertEquals("article-id", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void should_be_equal_by_id() {
    Comment c1 = new Comment("body1", "u1", "a1");
    Comment c2 = new Comment("body2", "u2", "a2");
    assertNotEquals(c1, c2);
    assertEquals(c1, c1);
  }

  @Test
  void should_create_with_no_arg_constructor() {
    Comment comment = new Comment();
    assertNull(comment.getId());
    assertNull(comment.getBody());
  }

  @Test
  void should_test_comment_hashcode_and_equals() {
    Comment c1 = new Comment("body1", "u1", "a1");
    assertNotEquals(c1, null);
    assertNotEquals(c1, "string");
    assertNotNull(c1.hashCode());
    assertNotNull(c1.toString());
  }

  @Test
  void should_have_created_at() {
    Comment comment = new Comment("body", "user-id", "article-id");
    assertNotNull(comment.getCreatedAt());
    assertEquals("user-id", comment.getUserId());
    assertEquals("article-id", comment.getArticleId());
  }
}
