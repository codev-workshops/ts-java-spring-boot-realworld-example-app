package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  void constructor_sets_fields() {
    ArticleFavorite fav = new ArticleFavorite("article-id", "user-id");

    assertEquals("article-id", fav.getArticleId());
    assertEquals("user-id", fav.getUserId());
  }

  @Test
  void equals_and_hashcode_work() {
    ArticleFavorite fav1 = new ArticleFavorite("a1", "u1");
    ArticleFavorite fav2 = new ArticleFavorite("a1", "u1");

    assertEquals(fav1, fav2);
    assertEquals(fav1.hashCode(), fav2.hashCode());
  }

  @Test
  void not_equal_when_different_fields() {
    ArticleFavorite fav1 = new ArticleFavorite("a1", "u1");
    ArticleFavorite fav2 = new ArticleFavorite("a2", "u1");

    assertNotEquals(fav1, fav2);
  }
}
