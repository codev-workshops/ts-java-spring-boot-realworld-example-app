package io.spring.application.article;

import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
  ArticleQueryService.class,
  MyBatisUserRepository.class,
  MyBatisArticleRepository.class,
  MyBatisArticleFavoriteRepository.class
})
public class ArticleQueryServiceTest extends DbTestBase {
  @Autowired private ArticleQueryService queryService;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
    userRepository.save(user);
    article =
        new Article(
            "test", "desc", "body", Arrays.asList("java", "spring"), user.getId(), new DateTime());
    articleRepository.save(article);
  }

  @Test
  public void should_fetch_article_success() {
    Optional<ArticleData> optional = queryService.findById(article.getId(), user);
    Assertions.assertTrue(optional.isPresent());

    ArticleData fetched = optional.get();
    Assertions.assertEquals(fetched.getFavoritesCount(), 0);
    Assertions.assertFalse(fetched.isFavorited());
    Assertions.assertNotNull(fetched.getCreatedAt());
    Assertions.assertNotNull(fetched.getUpdatedAt());
    Assertions.assertTrue(fetched.getTagList().contains("java"));
  }

  @Test
  public void should_get_article_with_right_favorite_and_favorite_count() {
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    userRepository.save(anotherUser);
    articleFavoriteRepository.save(new ArticleFavorite(article.getId(), anotherUser.getId()));

    Optional<ArticleData> optional = queryService.findById(article.getId(), anotherUser);
    Assertions.assertTrue(optional.isPresent());

    ArticleData articleData = optional.get();
    Assertions.assertEquals(articleData.getFavoritesCount(), 1);
    Assertions.assertTrue(articleData.isFavorited());
  }

  @Test
  public void should_get_default_article_list() {
    Article anotherArticle =
        new Article(
            "new article",
            "desc",
            "body",
            Arrays.asList("test"),
            user.getId(),
            new DateTime().minusHours(1));
    articleRepository.save(anotherArticle);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), user);
    Assertions.assertEquals(recentArticles.getCount(), 2);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 2);
    Assertions.assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

    ArticleDataList nodata =
        queryService.findRecentArticles(null, null, null, new Page(2, 10), user);
    Assertions.assertEquals(nodata.getCount(), 2);
    Assertions.assertEquals(nodata.getArticleDatas().size(), 0);
  }

  @Test
  public void should_get_default_article_list_by_cursor() {
    Article anotherArticle =
        new Article(
            "new article",
            "desc",
            "body",
            Arrays.asList("test"),
            user.getId(),
            new DateTime().minusHours(1));
    articleRepository.save(anotherArticle);

    CursorPager<ArticleData> recentArticles =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 20, Direction.NEXT), user);
    Assertions.assertEquals(recentArticles.getData().size(), 2);
    Assertions.assertEquals(recentArticles.getData().get(0).getId(), article.getId());

    CursorPager<ArticleData> nodata =
        queryService.findRecentArticlesWithCursor(
            null,
            null,
            null,
            new CursorPageParameter<DateTime>(
                DateTimeCursor.parse(recentArticles.getEndCursor().toString()), 20, Direction.NEXT),
            user);
    Assertions.assertEquals(nodata.getData().size(), 0);
    Assertions.assertEquals(nodata.getStartCursor(), null);

    CursorPager<ArticleData> prevArticles =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 20, Direction.PREV), user);
    Assertions.assertEquals(prevArticles.getData().size(), 2);
  }

  @Test
  public void should_query_article_by_author() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    Article anotherArticle =
        new Article("new article", "desc", "body", Arrays.asList("test"), anotherUser.getId());
    articleRepository.save(anotherArticle);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, user.getUsername(), null, new Page(), user);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 1);
    Assertions.assertEquals(recentArticles.getCount(), 1);
  }

  @Test
  public void should_query_article_by_favorite() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    Article anotherArticle =
        new Article("new article", "desc", "body", Arrays.asList("test"), anotherUser.getId());
    articleRepository.save(anotherArticle);

    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), anotherUser.getId());
    articleFavoriteRepository.save(articleFavorite);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(
            null, null, anotherUser.getUsername(), new Page(), anotherUser);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 1);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    ArticleData articleData = recentArticles.getArticleDatas().get(0);
    Assertions.assertEquals(articleData.getId(), article.getId());
    Assertions.assertEquals(articleData.getFavoritesCount(), 1);
    Assertions.assertTrue(articleData.isFavorited());
  }

  @Test
  public void should_query_article_by_tag() {
    Article anotherArticle =
        new Article("new article", "desc", "body", Arrays.asList("test"), user.getId());
    articleRepository.save(anotherArticle);

    ArticleDataList recentArticles =
        queryService.findRecentArticles("spring", null, null, new Page(), user);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 1);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    Assertions.assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

    ArticleDataList notag = queryService.findRecentArticles("notag", null, null, new Page(), user);
    Assertions.assertEquals(notag.getCount(), 0);
  }

  @Test
  public void should_show_following_if_user_followed_author() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
    userRepository.saveRelation(followRelation);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), anotherUser);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    ArticleData articleData = recentArticles.getArticleDatas().get(0);
    Assertions.assertTrue(articleData.getProfileData().isFollowing());
  }

  @Test
  public void should_get_user_feed() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
    userRepository.saveRelation(followRelation);

    ArticleDataList userFeed = queryService.findUserFeed(user, new Page());
    Assertions.assertEquals(userFeed.getCount(), 0);

    ArticleDataList anotherUserFeed = queryService.findUserFeed(anotherUser, new Page());
    Assertions.assertEquals(anotherUserFeed.getCount(), 1);
    ArticleData articleData = anotherUserFeed.getArticleDatas().get(0);
    Assertions.assertTrue(articleData.getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_when_article_id_not_found() {
    Optional<ArticleData> optional = queryService.findById("non-existent-id", user);
    Assertions.assertFalse(optional.isPresent());
  }

  @Test
  public void should_find_article_by_id_without_user() {
    Optional<ArticleData> optional = queryService.findById(article.getId(), null);
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertFalse(optional.get().isFavorited());
  }

  @Test
  public void should_find_article_by_slug() {
    Optional<ArticleData> optional = queryService.findBySlug(article.getSlug(), user);
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get().getId(), article.getId());
  }

  @Test
  public void should_return_empty_when_slug_not_found() {
    Optional<ArticleData> optional = queryService.findBySlug("non-existent-slug", user);
    Assertions.assertFalse(optional.isPresent());
  }

  @Test
  public void should_find_article_by_slug_without_user() {
    Optional<ArticleData> optional = queryService.findBySlug(article.getSlug(), null);
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertFalse(optional.get().isFavorited());
  }

  @Test
  public void should_get_empty_feed_with_cursor_when_no_follows() {
    CursorPager<ArticleData> feed =
        queryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertEquals(feed.getData().size(), 0);
    Assertions.assertFalse(feed.hasNext());
  }

  @Test
  public void should_get_user_feed_with_cursor() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);
    FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
    userRepository.saveRelation(followRelation);

    CursorPager<ArticleData> feed =
        queryService.findUserFeedWithCursor(
            anotherUser, new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertEquals(feed.getData().size(), 1);
    Assertions.assertEquals(feed.getData().get(0).getId(), article.getId());
  }

  @Test
  public void should_get_user_feed_with_cursor_prev_direction() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);
    FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
    userRepository.saveRelation(followRelation);

    CursorPager<ArticleData> feed =
        queryService.findUserFeedWithCursor(
            anotherUser, new CursorPageParameter<>(null, 20, Direction.PREV));
    Assertions.assertEquals(feed.getData().size(), 1);
  }

  @Test
  public void should_get_user_feed_with_cursor_has_extra() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);
    FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
    userRepository.saveRelation(followRelation);

    Article a1 =
        new Article(
            "a1", "desc", "body", Arrays.asList("t1"), user.getId(),
            new DateTime().plusHours(3));
    articleRepository.save(a1);
    Article a2 =
        new Article(
            "a2", "desc", "body", Arrays.asList("t2"), user.getId(),
            new DateTime().plusHours(2));
    articleRepository.save(a2);
    Article a3 =
        new Article(
            "a3", "desc", "body", Arrays.asList("t3"), user.getId(),
            new DateTime().plusHours(1));
    articleRepository.save(a3);

    CursorPager<ArticleData> feed =
        queryService.findUserFeedWithCursor(
            anotherUser, new CursorPageParameter<>(null, 2, Direction.NEXT));
    Assertions.assertEquals(2, feed.getData().size());
    Assertions.assertTrue(feed.hasNext());
  }

  @Test
  public void should_get_recent_articles_with_cursor_has_extra() {
    Article secondArticle =
        new Article("second", "desc", "body", Arrays.asList("test"), user.getId(), new DateTime().minusHours(1));
    articleRepository.save(secondArticle);

    CursorPager<ArticleData> articles =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 1, Direction.NEXT), user);
    Assertions.assertEquals(articles.getData().size(), 1);
    Assertions.assertTrue(articles.hasNext());
  }

  @Test
  public void should_get_recent_articles_without_user() {
    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), null);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    Assertions.assertFalse(recentArticles.getArticleDatas().get(0).isFavorited());
  }

  @Test
  public void should_get_recent_articles_with_cursor_without_user() {
    CursorPager<ArticleData> articles =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 20, Direction.NEXT), null);
    Assertions.assertEquals(articles.getData().size(), 1);
    Assertions.assertFalse(articles.getData().get(0).isFavorited());
  }
}
