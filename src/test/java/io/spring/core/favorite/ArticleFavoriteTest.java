package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite_with_ids() {
    ArticleFavorite favorite = new ArticleFavorite("article1", "user1");
    assertEquals("article1", favorite.getArticleId());
    assertEquals("user1", favorite.getUserId());
  }
}
