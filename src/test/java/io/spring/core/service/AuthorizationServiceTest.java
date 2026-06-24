package io.spring.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void canWriteArticle_authorUser_returnsTrue() {
    User user = new User("test@example.com", "testuser", "pass", "bio", "img");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    assertThat(AuthorizationService.canWriteArticle(user, article), is(true));
  }

  @Test
  public void canWriteArticle_nonAuthorUser_returnsFalse() {
    User user = new User("test@example.com", "testuser", "pass", "bio", "img");
    User other = new User("other@example.com", "other", "pass", "bio", "img");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), other.getId());
    assertThat(AuthorizationService.canWriteArticle(user, article), is(false));
  }

  @Test
  public void canWriteComment_articleAuthor_returnsTrue() {
    User user = new User("test@example.com", "testuser", "pass", "bio", "img");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    Comment comment = new Comment("comment body", "otherUserId", article.getId());
    assertThat(AuthorizationService.canWriteComment(user, article, comment), is(true));
  }

  @Test
  public void canWriteComment_commentAuthor_returnsTrue() {
    User user = new User("test@example.com", "testuser", "pass", "bio", "img");
    User articleOwner = new User("owner@example.com", "owner", "pass", "bio", "img");
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), articleOwner.getId());
    Comment comment = new Comment("comment body", user.getId(), article.getId());
    assertThat(AuthorizationService.canWriteComment(user, article, comment), is(true));
  }

  @Test
  public void canWriteComment_neitherAuthor_returnsFalse() {
    User user = new User("test@example.com", "testuser", "pass", "bio", "img");
    User articleOwner = new User("owner@example.com", "owner", "pass", "bio", "img");
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), articleOwner.getId());
    Comment comment = new Comment("comment body", "someOtherUser", article.getId());
    assertThat(AuthorizationService.canWriteComment(user, article, comment), is(false));
  }
}
