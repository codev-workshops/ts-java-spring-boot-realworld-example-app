package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ArticleQueryServiceUnitTest {

  private ArticleReadService articleReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private ArticleQueryService articleQueryService;

  @BeforeEach
  public void setUp() {
    articleReadService = Mockito.mock(ArticleReadService.class);
    userRelationshipQueryService = Mockito.mock(UserRelationshipQueryService.class);
    articleFavoritesReadService = Mockito.mock(ArticleFavoritesReadService.class);
    articleQueryService =
        new ArticleQueryService(
            articleReadService, userRelationshipQueryService, articleFavoritesReadService);
  }

  private ArticleData createArticle(String id) {
    ProfileData profile = new ProfileData("authorId", "author", "bio", "img", false);
    return new ArticleData(
        id, "slug", "title", "desc", "body", false, 0,
        new DateTime(1000L), new DateTime(2000L), Arrays.asList("tag"), profile);
  }

  @Test
  public void testFindByIdReturnsEmptyWhenNotFound() {
    when(articleReadService.findById("nonexistent")).thenReturn(null);
    User user = new User("a@test.com", "a", "123", "", "");
    Optional<ArticleData> result = articleQueryService.findById("nonexistent", user);
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindByIdWithNullUser() {
    ArticleData article = createArticle("a1");
    when(articleReadService.findById("a1")).thenReturn(article);

    Optional<ArticleData> result = articleQueryService.findById("a1", null);
    assertTrue(result.isPresent());
    assertFalse(result.get().isFavorited());
  }

  @Test
  public void testFindByIdWithUser() {
    ArticleData article = createArticle("a1");
    when(articleReadService.findById("a1")).thenReturn(article);
    when(articleFavoritesReadService.isUserFavorite(anyString(), eq("a1"))).thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount("a1")).thenReturn(3);
    when(userRelationshipQueryService.isUserFollowing(anyString(), eq("authorId"))).thenReturn(true);

    User user = new User("u@test.com", "u", "123", "", "");
    Optional<ArticleData> result = articleQueryService.findById("a1", user);
    assertTrue(result.isPresent());
    assertTrue(result.get().isFavorited());
    assertEquals(3, result.get().getFavoritesCount());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  public void testFindBySlugReturnsEmptyWhenNotFound() {
    when(articleReadService.findBySlug("nonexistent")).thenReturn(null);
    User user = new User("a@test.com", "a", "123", "", "");
    Optional<ArticleData> result = articleQueryService.findBySlug("nonexistent", user);
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindBySlugWithNullUser() {
    ArticleData article = createArticle("a1");
    when(articleReadService.findBySlug("slug")).thenReturn(article);

    Optional<ArticleData> result = articleQueryService.findBySlug("slug", null);
    assertTrue(result.isPresent());
  }

  @Test
  public void testFindBySlugWithUser() {
    ArticleData article = createArticle("a1");
    when(articleReadService.findBySlug("slug")).thenReturn(article);
    when(articleFavoritesReadService.isUserFavorite(anyString(), eq("a1"))).thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount("a1")).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    User user = new User("u@test.com", "u", "123", "", "");
    Optional<ArticleData> result = articleQueryService.findBySlug("slug", user);
    assertTrue(result.isPresent());
  }

  @Test
  public void testFindRecentArticlesWithCursorEmptyResult() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), eq(page)))
        .thenReturn(new ArrayList<>());

    User user = new User("u@test.com", "u", "123", "", "");
    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);
    assertEquals(0, result.getData().size());
  }

  @Test
  public void testFindRecentArticlesWithCursorHasExtra() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), eq(page)))
        .thenReturn(new ArrayList<>(Arrays.asList("a1", "a2")));
    ArticleData article = createArticle("a1");
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(article));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 1)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>());

    User user = new User("u@test.com", "u", "123", "", "");
    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);
    assertTrue(result.hasNext());
  }

  @Test
  public void testFindRecentArticlesWithCursorPrevDirection() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.PREV);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), eq(page)))
        .thenReturn(new ArrayList<>(Arrays.asList("a1")));
    ArticleData article = createArticle("a1");
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(article));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>());

    User user = new User("u@test.com", "u", "123", "", "");
    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);
    assertFalse(result.hasNext());
  }

  @Test
  public void testFindRecentArticlesWithCursorNullUser() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), eq(page)))
        .thenReturn(new ArrayList<>(Arrays.asList("a1")));
    ArticleData article = createArticle("a1");
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(article));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 2)));

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);
    assertEquals(1, result.getData().size());
  }

  @Test
  public void testFindUserFeedWithCursorNoFollowedUsers() {
    User user = new User("u@test.com", "u", "123", "", "");
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(new ArrayList<>());

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.NEXT);
    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);
    assertEquals(0, result.getData().size());
  }

  @Test
  public void testFindUserFeedWithCursorHasExtra() {
    User user = new User("u@test.com", "u", "123", "", "");
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Arrays.asList("followed1"));

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    ArticleData a1 = createArticle("a1");
    ArticleData a2 = createArticle("a2");
    when(articleReadService.findArticlesOfAuthorsWithCursor(anyList(), eq(page)))
        .thenReturn(new ArrayList<>(Arrays.asList(a1, a2)));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0), new ArticleFavoriteCount("a2", 0)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);
    assertTrue(result.hasNext());
    assertEquals(1, result.getData().size());
  }

  @Test
  public void testFindUserFeedWithCursorPrevDirection() {
    User user = new User("u@test.com", "u", "123", "", "");
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Arrays.asList("followed1"));

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.PREV);
    ArticleData a1 = createArticle("a1");
    when(articleReadService.findArticlesOfAuthorsWithCursor(anyList(), eq(page)))
        .thenReturn(new ArrayList<>(Arrays.asList(a1)));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);
    assertEquals(1, result.getData().size());
  }

  @Test
  public void testFindRecentArticlesEmpty() {
    Page page = new Page();
    when(articleReadService.queryArticles(any(), any(), any(), eq(page)))
        .thenReturn(new ArrayList<>());
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(0);

    User user = new User("u@test.com", "u", "123", "", "");
    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, user);
    assertEquals(0, result.getCount());
    assertEquals(0, result.getArticleDatas().size());
  }

  @Test
  public void testFindRecentArticlesWithNullUser() {
    Page page = new Page();
    when(articleReadService.queryArticles(any(), any(), any(), eq(page)))
        .thenReturn(Arrays.asList("a1"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    ArticleData article = createArticle("a1");
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(article));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 2)));

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, null);
    assertEquals(1, result.getCount());
    assertEquals(1, result.getArticleDatas().size());
  }

  @Test
  public void testFindRecentArticlesWithUserFollowingAuthor() {
    Page page = new Page();
    when(articleReadService.queryArticles(any(), any(), any(), eq(page)))
        .thenReturn(Arrays.asList("a1"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    ArticleData article = createArticle("a1");
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(article));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 1)));
    when(articleFavoritesReadService.userFavorites(anyList(), any()))
        .thenReturn(new HashSet<>(Arrays.asList("a1")));
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("authorId")));

    User user = new User("u@test.com", "u", "123", "", "");
    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, user);
    assertEquals(1, result.getArticleDatas().size());
    assertTrue(result.getArticleDatas().get(0).isFavorited());
    assertTrue(result.getArticleDatas().get(0).getProfileData().isFollowing());
  }

  @Test
  public void testFindUserFeedNoFollowedUsers() {
    User user = new User("u@test.com", "u", "123", "", "");
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(new ArrayList<>());

    Page page = new Page();
    ArticleDataList result = articleQueryService.findUserFeed(user, page);
    assertEquals(0, result.getCount());
    assertEquals(0, result.getArticleDatas().size());
  }

  @Test
  public void testFindUserFeedWithFollowedUsers() {
    User user = new User("u@test.com", "u", "123", "", "");
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Arrays.asList("followed1"));

    Page page = new Page();
    ArticleData article = createArticle("a1");
    when(articleReadService.findArticlesOfAuthors(anyList(), eq(page)))
        .thenReturn(Arrays.asList(article));
    when(articleReadService.countFeedSize(anyList())).thenReturn(1);
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>());

    ArticleDataList result = articleQueryService.findUserFeed(user, page);
    assertEquals(1, result.getCount());
    assertEquals(1, result.getArticleDatas().size());
  }
}
