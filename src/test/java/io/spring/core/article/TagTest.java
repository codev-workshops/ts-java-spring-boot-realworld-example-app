package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  void constructor_sets_name_and_generates_id() {
    Tag tag = new Tag("java");

    assertNotNull(tag.getId());
    assertFalse(tag.getId().isEmpty());
    assertEquals("java", tag.getName());
  }

  @Test
  void constructor_generates_unique_ids() {
    Tag t1 = new Tag("java");
    Tag t2 = new Tag("kotlin");

    assertNotEquals(t1.getId(), t2.getId());
  }

  @Test
  void tags_with_same_name_are_equal() {
    Tag t1 = new Tag("java");
    Tag t2 = new Tag("java");

    assertEquals(t1, t2);
  }

  @Test
  void tags_with_different_names_are_not_equal() {
    Tag t1 = new Tag("java");
    Tag t2 = new Tag("kotlin");

    assertNotEquals(t1, t2);
  }
}
