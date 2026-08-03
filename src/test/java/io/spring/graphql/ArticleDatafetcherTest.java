package io.spring.graphql;

import static io.spring.graphql.GraphQLTestFixtures.anonymous;
import static io.spring.graphql.GraphQLTestFixtures.articleData;
import static io.spring.graphql.GraphQLTestFixtures.authenticate;
import static io.spring.graphql.GraphQLTestFixtures.clearAuthentication;
import static io.spring.graphql.GraphQLTestFixtures.commentData;
import static io.spring.graphql.GraphQLTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
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
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;

  private ArticleDatafetcher datafetcher;
  private User currentUser;

  @BeforeEach
  void setUp() {
    datafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    currentUser = user("jack");
    authenticate(currentUser);
  }

  @AfterEach
  void tearDown() {
    clearAuthentication();
  }

  private CursorPager<ArticleData> pagerOf(ArticleData... articles) {
    return new CursorPager<>(Arrays.asList(articles), Direction.NEXT, false);
  }

  @Test
  void getFeedWithFirstReturnsConnection() {
    ArticleData article = articleData("a1", "slug-one");
    when(articleQueryService.findUserFeedWithCursor(eq(currentUser), any()))
        .thenReturn(pagerOf(article));

    DataFetcherResult<ArticlesConnection> result = datafetcher.getFeed(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
    assertThat(result.getData().getEdges().get(0).getNode().getSlug()).isEqualTo("slug-one");
    assertThat(result.getData().getPageInfo().isHasNextPage()).isFalse();
    @SuppressWarnings("unchecked")
    Map<String, ArticleData> localContext = (Map<String, ArticleData>) result.getLocalContext();
    assertThat(localContext).containsKey("slug-one");
  }

  @Test
  void getFeedWithLastUsesPrevDirection() {
    ArticleData article = articleData("a1", "slug-one");
    when(articleQueryService.findUserFeedWithCursor(
            eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(new CursorPager<>(Arrays.asList(article), Direction.PREV, true));

    DataFetcherResult<ArticlesConnection> result =
        datafetcher.getFeed(null, null, 10, String.valueOf(new DateTime().getMillis()), dfe);

    assertThat(result.getData().getPageInfo().isHasPreviousPage()).isTrue();
  }

  @Test
  void getFeedWithoutFirstAndLastThrows() {
    assertThatThrownBy(() -> datafetcher.getFeed(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getFeedWorksForAnonymousUser() {
    anonymous();
    when(articleQueryService.findUserFeedWithCursor(isNull(), any()))
        .thenReturn(pagerOf(articleData("a1", "slug-one")));

    DataFetcherResult<ArticlesConnection> result = datafetcher.getFeed(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userFeedResolvesTargetUser() {
    User target = user("target");
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    when(articleQueryService.findUserFeedWithCursor(eq(target), any()))
        .thenReturn(pagerOf(articleData("a1", "slug-one")));

    DataFetcherResult<ArticlesConnection> result = datafetcher.userFeed(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userFeedWithLastUsesPrevDirection() {
    User target = user("target");
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    when(articleQueryService.findUserFeedWithCursor(eq(target), any()))
        .thenReturn(pagerOf(articleData("a1", "slug-one")));

    DataFetcherResult<ArticlesConnection> result = datafetcher.userFeed(null, null, 5, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userFeedThrowsWhenUserMissing() {
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("ghost").build());
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> datafetcher.userFeed(10, null, null, null, dfe))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void userFeedWithoutFirstAndLastThrows() {
    assertThatThrownBy(() -> datafetcher.userFeed(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void userFavoritesReturnsConnection() {
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("target"), any(), eq(currentUser)))
        .thenReturn(pagerOf(articleData("a1", "slug-one")));

    DataFetcherResult<ArticlesConnection> result =
        datafetcher.userFavorites(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userFavoritesWithLastReturnsConnection() {
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("target"), any(), eq(currentUser)))
        .thenReturn(pagerOf(articleData("a1", "slug-one")));

    DataFetcherResult<ArticlesConnection> result =
        datafetcher.userFavorites(null, null, 10, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userFavoritesWithoutFirstAndLastThrows() {
    assertThatThrownBy(() -> datafetcher.userFavorites(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void userArticlesReturnsConnection() {
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq("target"), isNull(), any(), eq(currentUser)))
        .thenReturn(pagerOf(articleData("a1", "slug-one")));

    DataFetcherResult<ArticlesConnection> result =
        datafetcher.userArticles(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userArticlesWithLastReturnsConnection() {
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq("target"), isNull(), any(), eq(currentUser)))
        .thenReturn(pagerOf(articleData("a1", "slug-one")));

    DataFetcherResult<ArticlesConnection> result =
        datafetcher.userArticles(null, null, 10, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userArticlesWithoutFirstAndLastThrows() {
    assertThatThrownBy(() -> datafetcher.userArticles(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getArticlesReturnsConnection() {
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("jack"), eq("jill"), any(), eq(currentUser)))
        .thenReturn(pagerOf(articleData("a1", "slug-one")));

    DataFetcherResult<ArticlesConnection> result =
        datafetcher.getArticles(10, null, null, null, "jack", "jill", "java", dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void getArticlesWithLastReturnsConnection() {
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("jack"), eq("jill"), any(), eq(currentUser)))
        .thenReturn(pagerOf(articleData("a1", "slug-one")));

    DataFetcherResult<ArticlesConnection> result =
        datafetcher.getArticles(null, null, 10, null, "jack", "jill", "java", dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void getArticlesWithoutFirstAndLastThrows() {
    assertThatThrownBy(() -> datafetcher.getArticles(null, null, null, null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getArticlesWithEmptyResultHasNullCursors() {
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), isNull(), any(), eq(currentUser)))
        .thenReturn(new CursorPager<ArticleData>(Collections.emptyList(), Direction.NEXT, false));

    DataFetcherResult<ArticlesConnection> result =
        datafetcher.getArticles(10, null, null, null, null, null, null, dfe);

    assertThat(result.getData().getEdges()).isEmpty();
    assertThat(result.getData().getPageInfo().getStartCursor()).isNull();
    assertThat(result.getData().getPageInfo().getEndCursor()).isNull();
  }

  @Test
  void getArticleUsesLocalContextArticle() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "title", "desc", "body", Arrays.asList("java"), currentUser.getId());
    DataFetchingEnvironment environment = org.mockito.Mockito.mock(DataFetchingEnvironment.class);
    when(environment.<io.spring.core.article.Article>getLocalContext()).thenReturn(coreArticle);
    ArticleData articleData = articleData(coreArticle.getId(), "slug-one");
    when(articleQueryService.findById(coreArticle.getId(), currentUser))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = datafetcher.getArticle(environment);

    assertThat(result.getData().getSlug()).isEqualTo("slug-one");
    @SuppressWarnings("unchecked")
    Map<String, ArticleData> localContext = (Map<String, ArticleData>) result.getLocalContext();
    assertThat(localContext).containsKey("slug-one");
  }

  @Test
  void getArticleThrowsWhenNotFound() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "title", "desc", "body", Arrays.asList("java"), currentUser.getId());
    DataFetchingEnvironment environment = org.mockito.Mockito.mock(DataFetchingEnvironment.class);
    when(environment.<io.spring.core.article.Article>getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(coreArticle.getId(), currentUser))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> datafetcher.getArticle(environment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getCommentArticleUsesCommentLocalContext() {
    CommentData comment = commentData("c1", "a1");
    DataFetchingEnvironment environment = org.mockito.Mockito.mock(DataFetchingEnvironment.class);
    when(environment.<CommentData>getLocalContext()).thenReturn(comment);
    when(articleQueryService.findById("a1", currentUser))
        .thenReturn(Optional.of(articleData("a1", "slug-one")));

    DataFetcherResult<Article> result = datafetcher.getCommentArticle(environment);

    assertThat(result.getData().getSlug()).isEqualTo("slug-one");
  }

  @Test
  void getCommentArticleThrowsWhenArticleMissing() {
    CommentData comment = commentData("c1", "a1");
    DataFetchingEnvironment environment = org.mockito.Mockito.mock(DataFetchingEnvironment.class);
    when(environment.<CommentData>getLocalContext()).thenReturn(comment);
    when(articleQueryService.findById("a1", currentUser)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> datafetcher.getCommentArticle(environment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findArticleBySlugReturnsArticle() {
    when(articleQueryService.findBySlug("slug-one", currentUser))
        .thenReturn(Optional.of(articleData("a1", "slug-one")));

    DataFetcherResult<Article> result = datafetcher.findArticleBySlug("slug-one");

    assertThat(result.getData().getSlug()).isEqualTo("slug-one");
  }

  @Test
  void findArticleBySlugThrowsWhenMissing() {
    when(articleQueryService.findBySlug("nope", currentUser)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> datafetcher.findArticleBySlug("nope"))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
