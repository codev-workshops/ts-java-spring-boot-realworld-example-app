package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
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
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation commentMutation;
  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@email.com", "testuser", "pass", "", "");
    article = new Article("Title", "desc", "body", Collections.emptyList(), user.getId());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(u, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void createComment_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    CommentData commentData =
        new CommentData(
            "cid",
            "comment body",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), "testuser", "", "", false));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("title", "comment body");

    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void createComment_throws_when_not_authenticated() {
    setAnonymous();

    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("title", "body"));
  }

  @Test
  void removeComment_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("title", comment.getId());

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void removeComment_throws_when_not_authenticated() {
    setAnonymous();

    assertThrows(
        AuthenticationException.class, () -> commentMutation.removeComment("title", "cid"));
  }

  @Test
  void removeComment_throws_when_not_authorized() {
    setAuthenticated(user);
    User other = new User("other@test.com", "other", "pass", "", "");
    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    Article otherArticle =
        new Article("Title", "desc", "body", Collections.emptyList(), articleOwner.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(otherArticle));
    Comment comment = new Comment("body", other.getId(), otherArticle.getId());
    when(commentRepository.findById(otherArticle.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("title", comment.getId()));
  }
}
