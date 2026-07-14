package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CommentMutationTest extends GraphQLTestBase {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation commentMutation;
  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("a@test.com", "alice", "123", "", "");
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
  }

  @Test
  public void should_create_comment() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    CommentData commentData =
        new CommentData(
            "cid",
            "body",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData("1", "alice", "", "", false));
    when(commentQueryService.findById(any(), any())).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result = commentMutation.createComment("title", "body");
    assertTrue(result.getLocalContext() == commentData);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_throw_when_create_unauthenticated() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("title", "body"));
  }

  @Test
  public void should_throw_when_article_missing_on_create() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("title", "body"));
  }

  @Test
  public void should_remove_comment_when_authorized() {
    setAuthenticatedUser(user);
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus status = commentMutation.removeComment("title", comment.getId());
    assertTrue(status.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  public void should_throw_when_remove_not_authorized() {
    setAuthenticatedUser(user);
    User other = new User("o@test.com", "other", "123", "", "");
    Article othersArticle =
        new Article("title", "desc", "body", Arrays.asList("java"), other.getId());
    Comment comment = new Comment("body", other.getId(), othersArticle.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(othersArticle));
    when(commentRepository.findById(othersArticle.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("title", comment.getId()));
  }

  @Test
  public void should_throw_when_remove_comment_missing() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "cid")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment("title", "cid"));
  }
}
