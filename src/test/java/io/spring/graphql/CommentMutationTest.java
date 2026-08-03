package io.spring.graphql;

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
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentMutationTest extends GraphQLTestBase {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;
  @InjectMocks private CommentMutation commentMutation;

  private final Article article =
      new Article("title", "desc", "body", Arrays.asList("java"), CURRENT_USER.getId());

  @Test
  void should_create_comment() {
    authenticate(CURRENT_USER);
    CommentData commentData =
        new CommentData(
            "cid",
            "comment body",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData("id", "johnjacob", "bio", "image", false));
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(CURRENT_USER)))
        .thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("title", "comment body");

    verify(commentRepository).save(any(Comment.class));
    assertThat(result.getLocalContext()).isSameAs(commentData);
  }

  @Test
  void should_throw_when_created_comment_not_found() {
    authenticate(CURRENT_USER);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(CURRENT_USER))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentMutation.createComment("title", "body"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throw_when_commenting_on_missing_article() {
    authenticate(CURRENT_USER);
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentMutation.createComment("missing", "body"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throw_when_commenting_without_authentication() {
    anonymous();
    assertThatThrownBy(() -> commentMutation.createComment("title", "body"))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void should_remove_own_comment() {
    authenticate(CURRENT_USER);
    Comment comment = new Comment("body", CURRENT_USER.getId(), article.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus status = commentMutation.removeComment("title", comment.getId());

    assertThat(status.getSuccess()).isTrue();
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_when_removing_comment_of_another_user() {
    authenticate(CURRENT_USER);
    Article otherArticle =
        new Article("title", "desc", "body", Arrays.asList("java"), "other-user");
    Comment comment = new Comment("body", "another-user", otherArticle.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(otherArticle));
    when(commentRepository.findById(otherArticle.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentMutation.removeComment("title", comment.getId()))
        .isInstanceOf(NoAuthorizationException.class);
  }

  @Test
  void should_throw_when_removing_missing_comment() {
    authenticate(CURRENT_USER);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentMutation.removeComment("title", "missing"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throw_when_removing_without_authentication() {
    anonymous();
    assertThatThrownBy(() -> commentMutation.removeComment("title", "cid"))
        .isInstanceOf(AuthenticationException.class);
  }
}
