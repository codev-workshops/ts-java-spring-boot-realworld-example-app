package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the comments of a single article.
 *
 * <p>Handles requests under the base path {@code /articles/{slug}/comments}: adding a comment,
 * listing all comments of the article, and deleting a comment by id.
 */
@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;

  /**
   * Handles {@code POST /articles/{slug}/comments} and adds a comment to the article.
   *
   * @param slug the URL slug identifying the article being commented on
   * @param user the currently authenticated user, recorded as the comment's author
   * @param newCommentParam request body wrapped in a {@code "comment"} root element holding the
   *     non-blank comment body
   * @return {@code 201 Created} with a body of the form {@code {"comment": CommentData}}
   * @throws ResourceNotFoundException if no article with the given slug exists (mapped to {@code
   *     404 Not Found})
   * @throws org.springframework.web.bind.MethodArgumentNotValidException if the request body fails
   *     bean validation (rendered as {@code 422 Unprocessable Entity} by the global exception
   *     handler)
   */
  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment = new Comment(newCommentParam.getBody(), user.getId(), article.getId());
    commentRepository.save(comment);
    return ResponseEntity.status(201)
        .body(commentResponse(commentQueryService.findById(comment.getId(), user).get()));
  }

  /**
   * Handles {@code GET /articles/{slug}/comments} and lists all comments of the article.
   *
   * @param slug the URL slug identifying the article
   * @param user the currently authenticated user, or {@code null} for anonymous requests; when
   *     present each comment author's profile is enriched with the user's following state
   * @return {@code 200 OK} with a body of the form {@code {"comments": [CommentData, ...]}}
   * @throws ResourceNotFoundException if no article with the given slug exists (mapped to {@code
   *     404 Not Found})
   */
  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  /**
   * Handles {@code DELETE /articles/{slug}/comments/{id}} and removes a comment.
   *
   * @param slug the URL slug identifying the article the comment belongs to
   * @param commentId the id of the comment to delete
   * @param user the currently authenticated user; must be either the comment's author or the
   *     article's author
   * @return {@code 204 No Content} with an empty body
   * @throws ResourceNotFoundException if no article with the given slug exists, or no comment with
   *     the given id belongs to that article (mapped to {@code 404 Not Found})
   * @throws NoAuthorizationException if {@link AuthorizationService#canWriteComment} denies the
   *     user (mapped to {@code 403 Forbidden})
   */
  @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return commentRepository
        .findById(article.getId(), commentId)
        .map(
            comment -> {
              if (!AuthorizationService.canWriteComment(user, article, comment)) {
                throw new NoAuthorizationException();
              }
              commentRepository.remove(comment);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("comment")
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
