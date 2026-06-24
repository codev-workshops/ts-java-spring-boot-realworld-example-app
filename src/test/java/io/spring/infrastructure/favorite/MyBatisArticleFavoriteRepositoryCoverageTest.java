package io.spring.infrastructure.favorite;

import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisArticleFavoriteRepository.class})
public class MyBatisArticleFavoriteRepositoryCoverageTest extends DbTestBase {
  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  @Test
  public void should_return_empty_when_favorite_not_found() {
    Optional<?> result = articleFavoriteRepository.find("nonexistent-article", "nonexistent-user");
    Assertions.assertFalse(result.isPresent());
  }
}
