package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  void should_create_tag_with_name() {
    Tag tag = new Tag("java");
    assertEquals("java", tag.getName());
    assertNotNull(tag.getId());
  }

  @Test
  void should_be_equal_by_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1, tag2);
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  void should_not_be_equal_with_different_names() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("kotlin");
    assertNotEquals(tag1, tag2);
  }

  @Test
  void should_create_tag_with_no_arg_constructor() {
    Tag tag = new Tag();
    assertNull(tag.getName());
    assertNull(tag.getId());
  }

  @Test
  void should_not_equal_null() {
    Tag tag = new Tag("java");
    assertNotEquals(tag, null);
    assertNotEquals(tag, "string");
  }

  @Test
  void should_convert_title_to_slug() {
    assertEquals("hello-world", Article.toSlug("Hello World"));
    String slug = Article.toSlug("Test Title");
    assertEquals("test-title", slug);
  }

  @Test
  void should_create_article_and_test_getters() {
    DateTime createdAt = new DateTime(2023, 1, 1, 0, 0);
    Article article =
        new Article("Title", "desc", "body", java.util.Arrays.asList("tag1"), "user1", createdAt);
    assertEquals("title", article.getSlug());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertEquals("user1", article.getUserId());
    assertEquals(createdAt, article.getCreatedAt());
    assertEquals(createdAt, article.getUpdatedAt());
    assertNotNull(article.getId());
    assertEquals(1, article.getTags().size());
  }

  @Test
  void should_update_article_fields() {
    Article article =
        new Article("Title", "desc", "body", java.util.Collections.emptyList(), "user1");
    article.update("New Title", "new desc", "new body");
    assertEquals("new-title", article.getSlug());
    assertEquals("New Title", article.getTitle());
    assertEquals("new desc", article.getDescription());
    assertEquals("new body", article.getBody());
  }

  @Test
  void should_not_update_article_with_empty_values() {
    Article article =
        new Article("Title", "desc", "body", java.util.Collections.emptyList(), "user1");
    article.update("", "", "");
    assertEquals("title", article.getSlug());
    assertEquals("Title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
  }

  @Test
  void should_test_article_no_arg_constructor() {
    Article article = new Article();
    assertNull(article.getId());
    assertNull(article.getTitle());
  }

  @Test
  void should_test_article_equals_hashcode() {
    Article a1 = new Article("T1", "d", "b", java.util.Collections.emptyList(), "u1");
    assertEquals(a1, a1);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
    assertNotNull(a1.hashCode());
  }
}
