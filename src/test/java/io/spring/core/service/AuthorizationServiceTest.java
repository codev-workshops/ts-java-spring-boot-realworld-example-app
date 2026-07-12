package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void should_return_true_when_user_is_article_author() {
    User user = new User("a@test.com", "a", "123", "", "");
    Article article = new Article("title", "desc", "body", Collections.emptyList(), user.getId());

    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  public void should_return_false_when_user_is_not_article_author() {
    User author = new User("a@test.com", "a", "123", "", "");
    User other = new User("b@test.com", "b", "123", "", "");
    Article article = new Article("title", "desc", "body", Collections.emptyList(), author.getId());

    assertFalse(AuthorizationService.canWriteArticle(other, article));
  }

  @Test
  public void should_return_true_when_user_is_article_author_for_comment() {
    User author = new User("a@test.com", "a", "123", "", "");
    User commenter = new User("b@test.com", "b", "123", "", "");
    Article article = new Article("title", "desc", "body", Collections.emptyList(), author.getId());
    Comment comment = new Comment("comment", commenter.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(author, article, comment));
  }

  @Test
  public void should_return_true_when_user_is_comment_author() {
    User author = new User("a@test.com", "a", "123", "", "");
    User commenter = new User("b@test.com", "b", "123", "", "");
    Article article = new Article("title", "desc", "body", Collections.emptyList(), author.getId());
    Comment comment = new Comment("comment", commenter.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(commenter, article, comment));
  }

  @Test
  public void should_return_false_when_user_is_unrelated_to_comment() {
    User author = new User("a@test.com", "a", "123", "", "");
    User commenter = new User("b@test.com", "b", "123", "", "");
    User stranger = new User("c@test.com", "c", "123", "", "");
    Article article = new Article("title", "desc", "body", Collections.emptyList(), author.getId());
    Comment comment = new Comment("comment", commenter.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(stranger, article, comment));
  }
}
