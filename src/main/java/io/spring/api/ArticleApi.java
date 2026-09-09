package io.spring.api;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for operations on a single article, addressed by its slug.
 *
 * <p>Handles requests under the base path {@code /articles/{slug}}: reading, updating and deleting
 * one article. Listing and creation of articles are handled by {@link ArticlesApi}.
 */
@RestController
@RequestMapping(path = "/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {
  private ArticleQueryService articleQueryService;
  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  /**
   * Handles {@code GET /articles/{slug}} and returns a single article.
   *
   * @param slug the URL slug identifying the article
   * @param user the currently authenticated user, or {@code null} for anonymous requests; when
   *     present the returned article is enriched with the user's favorite/following state
   * @return {@code 200 OK} with a body of the form {@code {"article": ArticleData}}
   * @throws ResourceNotFoundException if no article with the given slug exists (mapped to {@code
   *     404 Not Found})
   */
  @GetMapping
  public ResponseEntity<?> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleQueryService
        .findBySlug(slug, user)
        .map(articleData -> ResponseEntity.ok(articleResponse(articleData)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Handles {@code PUT /articles/{slug}} and updates the title, description and/or body of an
   * existing article.
   *
   * @param slug the URL slug identifying the article to update
   * @param user the currently authenticated user; must be the author of the article
   * @param updateArticleParam request body wrapped in an {@code "article"} root element holding the
   *     new title, description and body
   * @return {@code 200 OK} with a body of the form {@code {"article": ArticleData}} describing the
   *     updated article (the slug may change if the title changed)
   * @throws ResourceNotFoundException if no article with the given slug exists (mapped to {@code
   *     404 Not Found})
   * @throws NoAuthorizationException if {@link AuthorizationService#canWriteArticle} denies the
   *     user, i.e. the user is not the article's author (mapped to {@code 403 Forbidden})
   */
  @PutMapping
  public ResponseEntity<?> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                throw new NoAuthorizationException();
              }
              Article updatedArticle =
                  articleCommandService.updateArticle(article, updateArticleParam);
              return ResponseEntity.ok(
                  articleResponse(
                      articleQueryService.findBySlug(updatedArticle.getSlug(), user).get()));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Handles {@code DELETE /articles/{slug}} and removes an article.
   *
   * @param slug the URL slug identifying the article to delete
   * @param user the currently authenticated user; must be the author of the article
   * @return {@code 204 No Content} with an empty body
   * @throws ResourceNotFoundException if no article with the given slug exists (mapped to {@code
   *     404 Not Found})
   * @throws NoAuthorizationException if {@link AuthorizationService#canWriteArticle} denies the
   *     user, i.e. the user is not the article's author (mapped to {@code 403 Forbidden})
   */
  @DeleteMapping
  public ResponseEntity deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                throw new NoAuthorizationException();
              }
              articleRepository.remove(article);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}
