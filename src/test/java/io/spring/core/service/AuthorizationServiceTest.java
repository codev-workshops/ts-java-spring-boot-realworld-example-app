package io.spring.core.service;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  private User newUser() {
    return new User("a@test.com", "a", "123", "", "");
  }

  @Test
  public void should_be_instantiable() {
    Assertions.assertNotNull(new AuthorizationService());
  }

  @Test
  public void should_allow_author_to_write_article() {
    User author = newUser();
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), author.getId());
    Assertions.assertTrue(AuthorizationService.canWriteArticle(author, article));
  }

  @Test
  public void should_not_allow_non_author_to_write_article() {
    User author = newUser();
    User other = newUser();
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), author.getId());
    Assertions.assertFalse(AuthorizationService.canWriteArticle(other, article));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    User articleAuthor = newUser();
    User commentAuthor = newUser();
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("content", commentAuthor.getId(), article.getId());
    Assertions.assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, comment));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User articleAuthor = newUser();
    User commentAuthor = newUser();
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("content", commentAuthor.getId(), article.getId());
    Assertions.assertTrue(AuthorizationService.canWriteComment(commentAuthor, article, comment));
  }

  @Test
  public void should_not_allow_stranger_to_write_comment() {
    User articleAuthor = newUser();
    User commentAuthor = newUser();
    User stranger = newUser();
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("content", commentAuthor.getId(), article.getId());
    Assertions.assertFalse(AuthorizationService.canWriteComment(stranger, article, comment));
  }
}
