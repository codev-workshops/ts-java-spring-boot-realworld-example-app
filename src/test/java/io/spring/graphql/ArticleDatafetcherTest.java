package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.ArrayList;
import java.util.Arrays;
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
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;

  private ArticleDatafetcher articleDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@email.com", "testuser", "pass", "", "");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private ArticleData buildArticleData(String slug) {
    return new ArticleData(
        "id-" + slug,
        slug,
        "Title " + slug,
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Collections.emptyList(),
        new ProfileData("uid", "testuser", "", "", false));
  }

  @Test
  void findArticleBySlug_returns_article() {
    ArticleData articleData = buildArticleData("test-slug");
    when(articleQueryService.findBySlug(eq("test-slug"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");

    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
  }

  @Test
  void findArticleBySlug_throws_when_not_found() {
    when(articleQueryService.findBySlug(eq("missing"), any())).thenReturn(Optional.empty());

    assertThrows(
        io.spring.api.exception.ResourceNotFoundException.class,
        () -> articleDatafetcher.findArticleBySlug("missing"));
  }

  @Test
  void getArticles_with_first_param() {
    ArticleData ad = buildArticleData("slug1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(ad), CursorPager.Direction.NEXT, true);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, null);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  void getArticles_with_last_param() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, null, null, null, null);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  void getArticles_throws_when_first_and_last_are_null() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, null));
  }

  @Test
  void getFeed_with_first_param() {
    ArticleData ad = buildArticleData("feed-slug");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(ad), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, null);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertFalse(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  void getFeed_with_last_param() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.PREV, true);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 5, null, null);

    assertNotNull(result);
    assertTrue(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  void getFeed_throws_when_first_and_last_are_null() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, null));
  }

  @Test
  void getArticle_from_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Title", "desc", "body", Collections.emptyList(), user.getId());
    ArticleData articleData = buildArticleData("title");
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dfe);

    assertNotNull(result);
    assertEquals("title", result.getData().getSlug());
  }

  @Test
  void getCommentArticle_returns_article() {
    CommentData commentData =
        new CommentData(
            "cid",
            "body",
            "aid1",
            new DateTime(),
            new DateTime(),
            new ProfileData("uid", "testuser", "", "", false));
    ArticleData articleData = buildArticleData("article-slug");
    when(articleQueryService.findById(eq("aid1"), any())).thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Article> result = articleDatafetcher.getCommentArticle(dfe);

    assertNotNull(result);
    assertEquals("article-slug", result.getData().getSlug());
  }

  @Test
  void userFeed_returns_connection() {
    User target = new User("target@email.com", "targetuser", "pass", "", "");
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    Profile profile = Profile.newBuilder().username("targetuser").build();
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void userFeed_with_last_param() {
    User target = new User("target@email.com", "targetuser", "pass", "", "");
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    Profile profile = Profile.newBuilder().username("targetuser").build();
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void userFavorites_returns_connection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void userFavorites_with_last_param() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void userArticles_returns_connection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void userArticles_with_last_param() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    Profile profile = Profile.newBuilder().username("testuser").build();
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
  }
}
