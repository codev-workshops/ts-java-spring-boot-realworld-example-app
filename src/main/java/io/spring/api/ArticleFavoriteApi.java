package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for favoriting and unfavoriting a single article.
 *
 * <p>Handles requests under the base path {@code articles/{slug}/favorite}. Both operations are
 * idempotent and return the article's current state as seen by the calling user.
 */
@RestController
@RequestMapping(path = "articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleRepository articleRepository;
  private ArticleQueryService articleQueryService;

  /**
   * Handles {@code POST /articles/{slug}/favorite} and marks the article as a favorite of the
   * current user.
   *
   * @param slug the URL slug identifying the article
   * @param user the currently authenticated user who is favoriting the article
   * @return {@code 200 OK} with a body of the form {@code {"article": ArticleData}} reflecting the
   *     updated favorite state and count
   * @throws ResourceNotFoundException if no article with the given slug exists (mapped to {@code
   *     404 Not Found})
   */
  @PostMapping
  public ResponseEntity favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
    articleFavoriteRepository.save(articleFavorite);
    return responseArticleData(articleQueryService.findBySlug(slug, user).get());
  }

  /**
   * Handles {@code DELETE /articles/{slug}/favorite} and removes the article from the current
   * user's favorites. If the article was not favorited by the user this is a no-op.
   *
   * @param slug the URL slug identifying the article
   * @param user the currently authenticated user who is unfavoriting the article
   * @return {@code 200 OK} with a body of the form {@code {"article": ArticleData}} reflecting the
   *     updated favorite state and count
   * @throws ResourceNotFoundException if no article with the given slug exists (mapped to {@code
   *     404 Not Found})
   */
  @DeleteMapping
  public ResponseEntity unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleFavoriteRepository
        .find(article.getId(), user.getId())
        .ifPresent(
            favorite -> {
              articleFavoriteRepository.remove(favorite);
            });
    return responseArticleData(articleQueryService.findBySlug(slug, user).get());
  }

  private ResponseEntity<HashMap<String, Object>> responseArticleData(
      final ArticleData articleData) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleData);
          }
        });
  }
}
