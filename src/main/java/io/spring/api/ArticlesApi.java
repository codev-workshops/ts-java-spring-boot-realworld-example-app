package io.spring.api;

import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.HashMap;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the article collection.
 *
 * <p>Handles requests under the base path {@code /articles}: creating a new article, listing recent
 * articles with optional filters, and the personalised feed of the current user. Operations on a
 * single article are handled by {@link ArticleApi}.
 */
@RestController
@RequestMapping(path = "/articles")
@AllArgsConstructor
public class ArticlesApi {
  private ArticleCommandService articleCommandService;
  private ArticleQueryService articleQueryService;

  /**
   * Handles {@code POST /articles} and creates a new article authored by the current user.
   *
   * @param newArticleParam request body wrapped in an {@code "article"} root element holding the
   *     title, description, body and optional tag list; validated with bean validation, including a
   *     duplicate-title check
   * @param user the currently authenticated user, recorded as the article's author
   * @return {@code 200 OK} with a body of the form {@code {"article": ArticleData}} describing the
   *     created article
   * @throws org.springframework.web.bind.MethodArgumentNotValidException if the request body fails
   *     bean validation (rendered as {@code 422 Unprocessable Entity} by the global exception
   *     handler)
   * @throws javax.validation.ConstraintViolationException if validation of the parameter fails
   *     again inside the {@code @Validated} {@link ArticleCommandService} (also rendered as {@code
   *     422})
   */
  @PostMapping
  public ResponseEntity createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam, @AuthenticationPrincipal User user) {
    Article article = articleCommandService.createArticle(newArticleParam, user);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleQueryService.findById(article.getId(), user).get());
          }
        });
  }

  /**
   * Handles {@code GET /articles/feed} and returns articles written by users the current user
   * follows, most recent first, using offset pagination.
   *
   * @param offset number of articles to skip; defaults to {@code 0}
   * @param limit maximum number of articles to return; defaults to {@code 20}
   * @param user the currently authenticated user whose feed is requested
   * @return {@code 200 OK} with an {@code ArticleDataList} body of the form {@code {"articles":
   *     [...], "articlesCount": n}}
   */
  @GetMapping(path = "feed")
  public ResponseEntity getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(articleQueryService.findUserFeed(user, new Page(offset, limit)));
  }

  /**
   * Handles {@code GET /articles} and returns recent articles, optionally filtered, using offset
   * pagination.
   *
   * @param offset number of articles to skip; defaults to {@code 0}
   * @param limit maximum number of articles to return; defaults to {@code 20}
   * @param tag optional tag name; only articles carrying this tag are returned
   * @param favoritedBy optional username; only articles favorited by this user are returned
   * @param author optional username; only articles written by this user are returned
   * @param user the currently authenticated user, or {@code null} for anonymous requests; when
   *     present each article is enriched with the user's favorite/following state
   * @return {@code 200 OK} with an {@code ArticleDataList} body of the form {@code {"articles":
   *     [...], "articlesCount": n}}
   */
  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(
            tag, author, favoritedBy, new Page(offset, limit), user));
  }
}
