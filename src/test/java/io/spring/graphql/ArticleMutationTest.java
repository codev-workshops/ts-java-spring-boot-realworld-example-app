package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation mutation;

  @BeforeEach
  void setUp() {
    mutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
  }

  @Test
  void testCreateArticleSuccess() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      CreateArticleInput input =
          CreateArticleInput.newBuilder()
              .title("title")
              .description("desc")
              .body("body")
              .tagList(Arrays.asList("tag1"))
              .build();

      Article article = new Article("title", "desc", "body", Arrays.asList("tag1"), user.getId());
      when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

      DataFetcherResult<ArticlePayload> result = mutation.createArticle(input);

      assertNotNull(result);
      assertNotNull(result.getData());
    }
  }

  @Test
  void testCreateArticleWithNullTagList() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      CreateArticleInput input =
          CreateArticleInput.newBuilder().title("title").description("desc").body("body").build();

      Article article = new Article("title", "desc", "body", Collections.emptyList(), user.getId());
      when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

      DataFetcherResult<ArticlePayload> result = mutation.createArticle(input);

      assertNotNull(result);
    }
  }

  @Test
  void testCreateArticleNotAuthenticated() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      CreateArticleInput input =
          CreateArticleInput.newBuilder().title("t").body("b").description("d").build();

      assertThrows(AuthenticationException.class, () -> mutation.createArticle(input));
    }
  }

  @Test
  void testUpdateArticleSuccess() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Article article = new Article("title", "desc", "body", Collections.emptyList(), user.getId());
      when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

      UpdateArticleInput params =
          UpdateArticleInput.newBuilder().title("new title").body("new body").build();

      when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

      DataFetcherResult<ArticlePayload> result = mutation.updateArticle("slug", params);

      assertNotNull(result);
    }
  }

  @Test
  void testUpdateArticleNotAuthorized() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      User otherUser = new User("other@test.com", "other", "pass", "bio", "img");
      Article article =
          new Article("title", "desc", "body", Collections.emptyList(), otherUser.getId());
      when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

      UpdateArticleInput params = UpdateArticleInput.newBuilder().title("new").build();

      assertThrows(NoAuthorizationException.class, () -> mutation.updateArticle("slug", params));
    }
  }

  @Test
  void testFavoriteArticleSuccess() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Article article = new Article("title", "desc", "body", Collections.emptyList(), user.getId());
      when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

      DataFetcherResult<ArticlePayload> result = mutation.favoriteArticle("slug");

      assertNotNull(result);
      verify(articleFavoriteRepository).save(any());
    }
  }

  @Test
  void testUnfavoriteArticleSuccess() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Article article = new Article("title", "desc", "body", Collections.emptyList(), user.getId());
      when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

      ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
      when(articleFavoriteRepository.find(article.getId(), user.getId()))
          .thenReturn(Optional.of(fav));

      DataFetcherResult<ArticlePayload> result = mutation.unfavoriteArticle("slug");

      assertNotNull(result);
      verify(articleFavoriteRepository).remove(fav);
    }
  }

  @Test
  void testDeleteArticleSuccess() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Article article = new Article("title", "desc", "body", Collections.emptyList(), user.getId());
      when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

      DeletionStatus result = mutation.deleteArticle("slug");

      assertNotNull(result);
      assertTrue(result.getSuccess());
      verify(articleRepository).remove(article);
    }
  }

  @Test
  void testDeleteArticleNotAuthorized() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      User user = new User("test@test.com", "testuser", "pass", "bio", "img");
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      User otherUser = new User("other@test.com", "other", "pass", "bio", "img");
      Article article =
          new Article("title", "desc", "body", Collections.emptyList(), otherUser.getId());
      when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

      assertThrows(NoAuthorizationException.class, () -> mutation.deleteArticle("slug"));
    }
  }
}
