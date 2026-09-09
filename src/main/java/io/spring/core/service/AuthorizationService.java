package io.spring.core.service;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;

/**
 * Domain-level authorization rules for write access to articles and comments.
 *
 * <p>Stateless; all rules are exposed as static methods and compare user ids only.
 */
public class AuthorizationService {
  /**
   * Decides whether a user may modify or delete an article. Only the article's author may.
   *
   * @param user the user attempting the write; must not be {@code null}
   * @param article the article being written to; must not be {@code null}
   * @return {@code true} if {@code user} is the author of {@code article}
   * @throws NullPointerException if {@code user} or {@code article} is {@code null}
   */
  public static boolean canWriteArticle(User user, Article article) {
    return user.getId().equals(article.getUserId());
  }

  /**
   * Decides whether a user may delete a comment. Both the comment's author and the author of the
   * article the comment belongs to may.
   *
   * @param user the user attempting the write; must not be {@code null}
   * @param article the article the comment belongs to; must not be {@code null}
   * @param comment the comment being written to; must not be {@code null}
   * @return {@code true} if {@code user} is the author of {@code article} or of {@code comment}
   * @throws NullPointerException if {@code user} or {@code article} is {@code null}, or if {@code
   *     comment} is {@code null} and {@code user} is not the article's author
   */
  public static boolean canWriteComment(User user, Article article, Comment comment) {
    return user.getId().equals(article.getUserId()) || user.getId().equals(comment.getUserId());
  }
}
