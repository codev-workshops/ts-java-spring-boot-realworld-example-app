package io.spring.application;

import static java.util.stream.Collectors.toList;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

/**
 * Read-side application service for articles.
 *
 * <p>Assembles {@link ArticleData} read models from the MyBatis read services and enriches them
 * with per-user information (favorite state, favorite count and whether the current user follows
 * the author). Supports both classic offset pagination and cursor based pagination.
 */
@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ArticleFavoritesReadService articleFavoritesReadService;

  /**
   * Finds a single article by its id.
   *
   * @param id the article id
   * @param user the current user, or {@code null} for anonymous access; when non-null the result is
   *     enriched with the user's favorite state, the favorite count and the following state of the
   *     author
   * @return the article read model, or {@link Optional#empty()} if no article has the given id
   */
  public Optional<ArticleData> findById(String id, User user) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (user != null) {
        fillExtraInfo(id, user, articleData);
      }
      return Optional.of(articleData);
    }
  }

  /**
   * Finds a single article by its URL slug.
   *
   * @param slug the article slug
   * @param user the current user, or {@code null} for anonymous access; when non-null the result is
   *     enriched with the user's favorite state, the favorite count and the following state of the
   *     author
   * @return the article read model, or {@link Optional#empty()} if no article has the given slug
   */
  public Optional<ArticleData> findBySlug(String slug, User user) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (user != null) {
        fillExtraInfo(articleData.getId(), user, articleData);
      }
      return Optional.of(articleData);
    }
  }

  /**
   * Lists recent articles matching the optional filters using cursor pagination.
   *
   * <p>The read service is asked for {@code page.getLimit() + 1} article ids (see {@link
   * CursorPageParameter#getQueryLimit()}). If that extra record comes back it is removed and used
   * only to signal that another page exists. When paging backwards ({@code !page.isNext()}) the
   * database returns rows in ascending order, so the list is reversed to restore newest-first
   * ordering. The resulting articles are enriched in place via {@code fillExtraInfo}.
   *
   * @param tag optional tag name filter, or {@code null}
   * @param author optional author username filter, or {@code null}
   * @param favoritedBy optional username filter for articles favorited by that user, or {@code
   *     null}
   * @param page cursor, direction and limit of the requested page
   * @param currentUser the current user, or {@code null} for anonymous access; when non-null each
   *     article is enriched with the user's favorite and following state
   * @return a pager holding the articles of the requested page, the paging direction and whether a
   *     further page exists in that direction; never {@code null}
   */
  public CursorPager<ArticleData> findRecentArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      CursorPageParameter<DateTime> page,
      User currentUser) {
    List<String> articleIds =
        articleReadService.findArticlesWithCursor(tag, author, favoritedBy, page);
    if (articleIds.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    } else {
      boolean hasExtra = articleIds.size() > page.getLimit();
      if (hasExtra) {
        articleIds.remove(page.getLimit());
      }
      if (!page.isNext()) {
        Collections.reverse(articleIds);
      }

      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUser);

      return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }
  }

  /**
   * Lists the feed of the given user, i.e. articles written by users they follow, using cursor
   * pagination.
   *
   * <p>Applies the same peek-ahead behaviour as {@link #findRecentArticlesWithCursor}: one extra
   * record beyond {@code page.getLimit()} is fetched to detect whether another page exists, then
   * removed; when paging backwards the list is reversed to keep newest-first ordering. Articles are
   * enriched in place via {@code fillExtraInfo}.
   *
   * @param user the authenticated user whose feed is requested; must not be {@code null}
   * @param page cursor, direction and limit of the requested page
   * @return a pager holding the articles of the requested page, the paging direction and whether a
   *     further page exists in that direction; empty if the user follows nobody
   * @throws NullPointerException if {@code user} is {@code null}
   */
  public CursorPager<ArticleData> findUserFeedWithCursor(
      User user, CursorPageParameter<DateTime> page) {
    List<String> followdUsers = userRelationshipQueryService.followedUsers(user.getId());
    if (followdUsers.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    } else {
      List<ArticleData> articles =
          articleReadService.findArticlesOfAuthorsWithCursor(followdUsers, page);
      boolean hasExtra = articles.size() > page.getLimit();
      if (hasExtra) {
        articles.remove(page.getLimit());
      }
      if (!page.isNext()) {
        Collections.reverse(articles);
      }
      fillExtraInfo(articles, user);
      return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }
  }

  /**
   * Lists recent articles matching the optional filters using offset pagination.
   *
   * @param tag optional tag name filter, or {@code null}
   * @param author optional author username filter, or {@code null}
   * @param favoritedBy optional username filter for articles favorited by that user, or {@code
   *     null}
   * @param page offset and limit of the requested page
   * @param currentUser the current user, or {@code null} for anonymous access; when non-null each
   *     article is enriched with the user's favorite and following state
   * @return the articles of the requested page together with the total count of articles matching
   *     the filters
   */
  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, User currentUser) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
    int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUser);
      return new ArticleDataList(articles, articleCount);
    }
  }

  /**
   * Lists the feed of the given user, i.e. articles written by users they follow, using offset
   * pagination.
   *
   * @param user the authenticated user whose feed is requested; must not be {@code null}
   * @param page offset and limit of the requested page
   * @return the articles of the requested page together with the total size of the feed; an empty
   *     list with count {@code 0} if the user follows nobody
   * @throws NullPointerException if {@code user} is {@code null}
   */
  public ArticleDataList findUserFeed(User user, Page page) {
    List<String> followdUsers = userRelationshipQueryService.followedUsers(user.getId());
    if (followdUsers.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followdUsers, page);
      fillExtraInfo(articles, user);
      int count = articleReadService.countFeedSize(followdUsers);
      return new ArticleDataList(articles, count);
    }
  }

  /**
   * Enriches a batch of articles in place. Always sets the favorite count; when a current user is
   * given also sets the favorited flag and the following flag of each author.
   *
   * @param articles the articles to mutate
   * @param currentUser the current user, or {@code null} for anonymous access
   */
  private void fillExtraInfo(List<ArticleData> articles, User currentUser) {
    setFavoriteCount(articles);
    if (currentUser != null) {
      setIsFavorite(articles, currentUser);
      setIsFollowingAuthor(articles, currentUser);
    }
  }

  /**
   * Marks, in place, the author profile of every article whose author is followed by the current
   * user, using a single batch lookup of all author ids.
   *
   * @param articles the articles to mutate
   * @param currentUser the current user; must not be {@code null}
   */
  private void setIsFollowingAuthor(List<ArticleData> articles, User currentUser) {
    Set<String> followingAuthors =
        userRelationshipQueryService.followingAuthors(
            currentUser.getId(),
            articles.stream()
                .map(articleData1 -> articleData1.getProfileData().getId())
                .collect(toList()));
    articles.forEach(
        articleData -> {
          if (followingAuthors.contains(articleData.getProfileData().getId())) {
            articleData.getProfileData().setFollowing(true);
          }
        });
  }

  /**
   * Sets the favorite count of every article in place, using a single batch lookup of all article
   * ids. Articles without any favorites receive a {@code null} count from the lookup map.
   *
   * @param articles the articles to mutate
   */
  private void setFavoriteCount(List<ArticleData> articles) {
    List<ArticleFavoriteCount> favoritesCounts =
        articleFavoritesReadService.articlesFavoriteCount(
            articles.stream().map(ArticleData::getId).collect(toList()));
    Map<String, Integer> countMap = new HashMap<>();
    favoritesCounts.forEach(
        item -> {
          countMap.put(item.getId(), item.getCount());
        });
    articles.forEach(
        articleData -> articleData.setFavoritesCount(countMap.get(articleData.getId())));
  }

  /**
   * Marks, in place, every article that the current user has favorited, using a single batch lookup
   * of all article ids.
   *
   * @param articles the articles to mutate
   * @param currentUser the current user; must not be {@code null}
   */
  private void setIsFavorite(List<ArticleData> articles, User currentUser) {
    Set<String> favoritedArticles =
        articleFavoritesReadService.userFavorites(
            articles.stream().map(articleData -> articleData.getId()).collect(toList()),
            currentUser);

    articles.forEach(
        articleData -> {
          if (favoritedArticles.contains(articleData.getId())) {
            articleData.setFavorited(true);
          }
        });
  }

  /**
   * Enriches a single article in place with the user's favorite state, the favorite count and
   * whether the user follows the author, using one lookup per attribute.
   *
   * @param id the article id
   * @param user the current user; must not be {@code null}
   * @param articleData the article to mutate
   */
  private void fillExtraInfo(String id, User user, ArticleData articleData) {
    articleData.setFavorited(articleFavoritesReadService.isUserFavorite(user.getId(), id));
    articleData.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(id));
    articleData
        .getProfileData()
        .setFollowing(
            userRelationshipQueryService.isUserFollowing(
                user.getId(), articleData.getProfileData().getId()));
  }
}
