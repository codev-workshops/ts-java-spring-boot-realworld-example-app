package io.spring.graphql;

import static io.spring.graphql.GraphQLTestFixtures.anonymous;
import static io.spring.graphql.GraphQLTestFixtures.authenticate;
import static io.spring.graphql.GraphQLTestFixtures.clearAuthentication;
import static io.spring.graphql.GraphQLTestFixtures.commentData;
import static io.spring.graphql.GraphQLTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation mutation;
  private User currentUser;
  private Article article;

  @BeforeEach
  void setUp() {
    mutation = new CommentMutation(articleRepository, commentRepository, commentQueryService);
    currentUser = user("jack");
    authenticate(currentUser);
    article = new Article("title", "desc", "body", Arrays.asList("java"), currentUser.getId());
  }

  @AfterEach
  void tearDown() {
    clearAuthentication();
  }

  @Test
  void createCommentSavesAndReturnsPayload() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));
    CommentData commentData = commentData("c1", article.getId());
    when(commentQueryService.findById(any(String.class), eq(currentUser)))
        .thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result = mutation.createComment("slug", "hi");

    verify(commentRepository).save(any(Comment.class));
    assertThat(result.getLocalContext()).isEqualTo(commentData);
  }

  @Test
  void createCommentThrowsWhenAnonymous() {
    anonymous();

    assertThatThrownBy(() -> mutation.createComment("slug", "hi"))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void createCommentThrowsWhenArticleMissing() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.createComment("slug", "hi"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createCommentThrowsWhenCommentNotReadable() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(String.class), eq(currentUser)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.createComment("slug", "hi"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void removeCommentDeletesOwnComment() {
    Comment comment = new Comment("body", currentUser.getId(), article.getId());
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus status = mutation.removeComment("slug", comment.getId());

    verify(commentRepository).remove(comment);
    assertThat(status.getSuccess()).isTrue();
  }

  @Test
  void removeCommentThrowsWhenNotAuthorized() {
    User other = user("other");
    Article othersArticle = new Article("t", "d", "b", Arrays.asList("java"), other.getId());
    Comment comment = new Comment("body", other.getId(), othersArticle.getId());
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(othersArticle));
    when(commentRepository.findById(othersArticle.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThatThrownBy(() -> mutation.removeComment("slug", comment.getId()))
        .isInstanceOf(NoAuthorizationException.class);
  }

  @Test
  void removeCommentThrowsWhenCommentMissing() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "c1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.removeComment("slug", "c1"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void removeCommentThrowsWhenArticleMissing() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.removeComment("slug", "c1"))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
