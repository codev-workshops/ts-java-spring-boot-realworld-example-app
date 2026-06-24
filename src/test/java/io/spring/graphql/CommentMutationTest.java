package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
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
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation mutation;

  @BeforeEach
  void setUp() {
    mutation = new CommentMutation(articleRepository, commentRepository, commentQueryService);
  }

  @Test
  void testCreateCommentSuccess() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Article article =
          new Article("title", "desc", "body", java.util.Collections.emptyList(), user.getId());
      when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

      CommentData commentData =
          new CommentData(
              "c1",
              "body",
              article.getId(),
              new DateTime(),
              new DateTime(),
              new ProfileData("userId", "author", "bio", "image", false));
      when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

      DataFetcherResult<CommentPayload> result = mutation.createComment("slug", "body");

      assertNotNull(result);
      verify(commentRepository).save(any());
    }
  }

  @Test
  void testCreateCommentNotAuthenticated() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(AuthenticationException.class, () -> mutation.createComment("slug", "body"));
    }
  }

  @Test
  void testRemoveCommentSuccess() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Article article =
          new Article("title", "desc", "body", java.util.Collections.emptyList(), user.getId());
      when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

      Comment comment = new Comment("comment body", user.getId(), article.getId());
      when(commentRepository.findById(article.getId(), "commentId"))
          .thenReturn(Optional.of(comment));

      DeletionStatus result = mutation.removeComment("slug", "commentId");

      assertNotNull(result);
      assertTrue(result.getSuccess());
      verify(commentRepository).remove(comment);
    }
  }

  @Test
  void testRemoveCommentNotAuthenticated() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> mutation.removeComment("slug", "commentId"));
    }
  }
}
