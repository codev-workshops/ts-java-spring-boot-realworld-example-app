package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  void should_create_article_favorite() {
    ArticleFavorite fav = new ArticleFavorite("article-id", "user-id");
    assertEquals("article-id", fav.getArticleId());
    assertEquals("user-id", fav.getUserId());
  }

  @Test
  void should_be_equal_by_all_fields() {
    ArticleFavorite fav1 = new ArticleFavorite("a1", "u1");
    ArticleFavorite fav2 = new ArticleFavorite("a1", "u1");
    assertEquals(fav1, fav2);
    assertEquals(fav1.hashCode(), fav2.hashCode());
  }

  @Test
  void should_not_be_equal_with_different_fields() {
    ArticleFavorite fav1 = new ArticleFavorite("a1", "u1");
    ArticleFavorite fav2 = new ArticleFavorite("a2", "u1");
    assertNotEquals(fav1, fav2);
  }

  @Test
  void should_create_with_no_arg_constructor() {
    ArticleFavorite fav = new ArticleFavorite();
    assertNull(fav.getArticleId());
    assertNull(fav.getUserId());
  }
}
