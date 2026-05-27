package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  void canWriteArticle_returns_true_when_user_is_author() {
    User user = new User("email@test.com", "user1", "pass", "", "");
    Article article = new Article("title", "desc", "body", Collections.emptyList(), user.getId());

    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  void canWriteArticle_returns_false_when_user_is_not_author() {
    User user = new User("email@test.com", "user1", "pass", "", "");
    User other = new User("other@test.com", "user2", "pass", "", "");
    Article article = new Article("title", "desc", "body", Collections.emptyList(), other.getId());

    assertFalse(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  void canWriteComment_returns_true_when_user_is_article_owner() {
    User user = new User("email@test.com", "user1", "pass", "", "");
    User commentAuthor = new User("other@test.com", "user2", "pass", "", "");
    Article article = new Article("title", "desc", "body", Collections.emptyList(), user.getId());
    Comment comment = new Comment("comment body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(user, article, comment));
  }

  @Test
  void canWriteComment_returns_true_when_user_is_comment_owner() {
    User user = new User("email@test.com", "user1", "pass", "", "");
    User articleAuthor = new User("other@test.com", "user2", "pass", "", "");
    Article article =
        new Article("title", "desc", "body", Collections.emptyList(), articleAuthor.getId());
    Comment comment = new Comment("comment body", user.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(user, article, comment));
  }

  @Test
  void canWriteComment_returns_false_when_user_is_neither_owner() {
    User user = new User("email@test.com", "user1", "pass", "", "");
    User articleAuthor = new User("other@test.com", "user2", "pass", "", "");
    User commentAuthor = new User("third@test.com", "user3", "pass", "", "");
    Article article =
        new Article("title", "desc", "body", Collections.emptyList(), articleAuthor.getId());
    Comment comment = new Comment("comment body", commentAuthor.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(user, article, comment));
  }
}
