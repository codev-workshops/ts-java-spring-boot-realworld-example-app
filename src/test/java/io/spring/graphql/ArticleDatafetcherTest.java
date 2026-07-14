package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.ArticlesConnection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ArticleDatafetcherTest extends GraphQLTestBase {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;
  @Mock private DataFetchingEnvironment env;

  private ArticleDatafetcher articleDatafetcher;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    setAnonymous();
  }

  private ArticleData articleData(String slug) {
    DateTime now = new DateTime();
    return new ArticleData(
        "aid-" + slug,
        slug,
        "title",
        "desc",
        "body",
        false,
        0,
        now,
        now,
        Collections.singletonList("java"),
        new ProfileData("1", "alice", "", "", false));
  }

  private CursorPager<ArticleData> pager(List<ArticleData> data, Direction direction) {
    return new CursorPager<>(data, direction, false);
  }

  @Test
  public void should_get_feed_first() {
    when(articleQueryService.findUserFeedWithCursor(isNull(), any(CursorPageParameter.class)))
        .thenReturn(pager(Collections.singletonList(articleData("s1")), Direction.NEXT));
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_feed_last() {
    when(articleQueryService.findUserFeedWithCursor(isNull(), any(CursorPageParameter.class)))
        .thenReturn(pager(Collections.emptyList(), Direction.PREV));
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_feed_no_first_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_feed() {
    User target = new User("b@test.com", "bob", "123", "", "");
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("bob").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
    when(articleQueryService.findUserFeedWithCursor(eq(target), any(CursorPageParameter.class)))
        .thenReturn(pager(Collections.singletonList(articleData("s1")), Direction.NEXT));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_user_feed_target_missing() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("bob").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  public void should_get_user_favorites() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("bob").build();
    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("bob"), any(CursorPageParameter.class), isNull()))
        .thenReturn(pager(Collections.singletonList(articleData("s1")), Direction.NEXT));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_favorites_last() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("bob").build();
    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("bob"), any(CursorPageParameter.class), isNull()))
        .thenReturn(pager(Collections.emptyList(), Direction.PREV));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dfe);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_articles() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("bob").build();
    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq("bob"), isNull(), any(CursorPageParameter.class), isNull()))
        .thenReturn(pager(Collections.singletonList(articleData("s1")), Direction.NEXT));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_articles() {
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("alice"), eq("bob"), any(CursorPageParameter.class), isNull()))
        .thenReturn(pager(Collections.singletonList(articleData("s1")), Direction.NEXT));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "alice", "bob", "java", dfe);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_articles_last() {
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), isNull(), any(CursorPageParameter.class), isNull()))
        .thenReturn(pager(Collections.emptyList(), Direction.PREV));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, null, null, null, dfe);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_articles_no_first_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  public void should_get_article_from_local_context() {
    io.spring.core.article.Article article =
        new io.spring.core.article.Article(
            "title", "desc", "body", Collections.singletonList("java"), "uid");
    when(env.getLocalContext()).thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), isNull()))
        .thenReturn(Optional.of(articleData(article.getSlug())));

    DataFetcherResult<io.spring.graphql.types.Article> result = articleDatafetcher.getArticle(env);
    assertEquals("title", result.getData().getTitle());
  }

  @Test
  public void should_throw_when_article_not_found() {
    io.spring.core.article.Article article =
        new io.spring.core.article.Article(
            "title", "desc", "body", Collections.singletonList("java"), "uid");
    when(env.getLocalContext()).thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), isNull())).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getArticle(env));
  }

  @Test
  public void should_get_comment_article() {
    CommentData comment =
        new CommentData(
            "cid",
            "body",
            "aid",
            new DateTime(),
            new DateTime(),
            new ProfileData("1", "alice", "", "", false));
    when(env.getLocalContext()).thenReturn(comment);
    when(articleQueryService.findById(eq("aid"), isNull()))
        .thenReturn(Optional.of(articleData("slug-1")));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getCommentArticle(env);
    assertEquals("title", result.getData().getTitle());
  }

  @Test
  public void should_find_article_by_slug() {
    when(articleQueryService.findBySlug(eq("slug-1"), isNull()))
        .thenReturn(Optional.of(articleData("slug-1")));
    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("slug-1");
    assertEquals("slug-1", result.getData().getSlug());
  }

  @Test
  public void should_throw_when_find_by_slug_missing() {
    when(articleQueryService.findBySlug(eq("slug-x"), isNull())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("slug-x"));
  }
}
