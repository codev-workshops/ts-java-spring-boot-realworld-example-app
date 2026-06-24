package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ArticleDatafetcher fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new ArticleDatafetcher(articleQueryService, userRepository);
  }

  private ArticleData createArticleData(String slug) {
    return new ArticleData(
        "id-" + slug,
        slug,
        "title",
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Collections.emptyList(),
        new io.spring.application.data.ProfileData("id", "author", "bio", "image", false));
  }

  @Test
  void testGetFeedWithFirst() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);

      when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result = fetcher.getFeed(10, null, null, null, dfe);

      assertNotNull(result);
      assertNotNull(result.getData());
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void testGetFeedWithLast() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.PREV, true);

      when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result = fetcher.getFeed(null, null, 10, null, dfe);

      assertNotNull(result);
      assertNotNull(result.getData());
    }
  }

  @Test
  void testGetFeedNoFirstOrLast() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void testUserFeedWithFirst() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Profile profile = Profile.newBuilder().username("user1").build();
      when(dfe.getSource()).thenReturn(profile);

      User target = new User("user1@test.com", "user1", "pass", "bio", "img");
      when(userRepository.findByUsername("user1")).thenReturn(Optional.of(target));

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
      when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result = fetcher.userFeed(10, null, null, null, dfe);

      assertNotNull(result);
    }
  }

  @Test
  void testUserFeedWithLast() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Profile profile = Profile.newBuilder().username("user1").build();
      when(dfe.getSource()).thenReturn(profile);

      User target = new User("user1@test.com", "user1", "pass", "bio", "img");
      when(userRepository.findByUsername("user1")).thenReturn(Optional.of(target));

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.PREV, true);
      when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result = fetcher.userFeed(null, null, 10, null, dfe);

      assertNotNull(result);
    }
  }

  @Test
  void testUserFeedNoFirstOrLast() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void testUserFeedUserNotFound() {
    Profile profile = Profile.newBuilder().username("nonexist").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("nonexist")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> fetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  void testUserFavoritesWithFirst() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Profile profile = Profile.newBuilder().username("user1").build();
      when(dfe.getSource()).thenReturn(profile);

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
      when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          fetcher.userFavorites(10, null, null, null, dfe);

      assertNotNull(result);
    }
  }

  @Test
  void testUserFavoritesWithLast() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Profile profile = Profile.newBuilder().username("user1").build();
      when(dfe.getSource()).thenReturn(profile);

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.PREV, true);
      when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          fetcher.userFavorites(null, null, 10, null, dfe);

      assertNotNull(result);
    }
  }

  @Test
  void testUserFavoritesNoFirstOrLast() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void testUserArticlesWithFirst() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Profile profile = Profile.newBuilder().username("user1").build();
      when(dfe.getSource()).thenReturn(profile);

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
      when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          fetcher.userArticles(10, null, null, null, dfe);

      assertNotNull(result);
    }
  }

  @Test
  void testUserArticlesWithLast() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Profile profile = Profile.newBuilder().username("user1").build();
      when(dfe.getSource()).thenReturn(profile);

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.PREV, true);
      when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          fetcher.userArticles(null, null, 10, null, dfe);

      assertNotNull(result);
    }
  }

  @Test
  void testUserArticlesNoFirstOrLast() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void testGetArticlesWithFirst() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
      when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          fetcher.getArticles(10, null, null, null, "author", "fav", "tag", dfe);

      assertNotNull(result);
    }
  }

  @Test
  void testGetArticlesWithLast() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      ArticleData articleData = createArticleData("slug1");
      CursorPager<ArticleData> pager =
          new CursorPager<>(Arrays.asList(articleData), Direction.PREV, true);
      when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
          .thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result =
          fetcher.getArticles(null, null, 10, null, "author", "fav", "tag", dfe);

      assertNotNull(result);
    }
  }

  @Test
  void testGetArticlesNoFirstOrLast() {
    assertThrows(
        IllegalArgumentException.class,
        () -> fetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void testGetArticle() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      io.spring.core.article.Article coreArticle =
          new io.spring.core.article.Article(
              "title", "desc", "body", Arrays.asList("tag"), "userId");
      when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);

      ArticleData articleData = createArticleData("slug1");
      when(articleQueryService.findById(eq(coreArticle.getId()), any()))
          .thenReturn(Optional.of(articleData));

      DataFetcherResult<Article> result = fetcher.getArticle(dataFetchingEnvironment);

      assertNotNull(result);
      assertNotNull(result.getData());
    }
  }

  @Test
  void testGetArticleNotFound() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      io.spring.core.article.Article coreArticle =
          new io.spring.core.article.Article(
              "title", "desc", "body", Collections.emptyList(), "userId");
      when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
      when(articleQueryService.findById(eq(coreArticle.getId()), any()))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class, () -> fetcher.getArticle(dataFetchingEnvironment));
    }
  }

  @Test
  void testGetCommentArticle() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      CommentData commentData =
          new CommentData(
              "commentId",
              "body",
              "articleId",
              new DateTime(),
              new DateTime(),
              new io.spring.application.data.ProfileData("id", "author", "bio", "image", false));
      when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

      ArticleData articleData = createArticleData("slug1");
      when(articleQueryService.findById(eq("articleId"), any()))
          .thenReturn(Optional.of(articleData));

      DataFetcherResult<Article> result = fetcher.getCommentArticle(dataFetchingEnvironment);

      assertNotNull(result);
      assertNotNull(result.getData());
    }
  }

  @Test
  void testFindArticleBySlug() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      ArticleData articleData = createArticleData("my-slug");
      when(articleQueryService.findBySlug(eq("my-slug"), any()))
          .thenReturn(Optional.of(articleData));

      DataFetcherResult<Article> result = fetcher.findArticleBySlug("my-slug");

      assertNotNull(result);
      assertNotNull(result.getData());
    }
  }

  @Test
  void testFindArticleBySlugNotFound() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(articleQueryService.findBySlug(eq("nonexist"), any())).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> fetcher.findArticleBySlug("nonexist"));
    }
  }

  @Test
  void testGetFeedWithNullCursors() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      CursorPager<ArticleData> pager =
          new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
      when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

      DataFetcherResult<ArticlesConnection> result = fetcher.getFeed(10, null, null, null, dfe);

      assertNotNull(result);
      assertNotNull(result.getData().getPageInfo());
    }
  }
}
