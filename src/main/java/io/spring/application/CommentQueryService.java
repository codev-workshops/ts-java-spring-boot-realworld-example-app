package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

/**
 * Read-side application service for comments.
 *
 * <p>Assembles {@link CommentData} read models from the MyBatis read service and enriches the
 * embedded author profile with whether the current user follows that author.
 */
@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  /**
   * Finds a single comment by its id.
   *
   * @param id the comment id
   * @param user the current user; must not be {@code null}. The author profile's {@code following}
   *     flag is set according to whether this user follows the author
   * @return the comment read model, or {@link Optional#empty()} if no comment has the given id
   * @throws NullPointerException if the comment exists and {@code user} is {@code null}
   */
  public Optional<CommentData> findById(String id, User user) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    } else {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
    }
    return Optional.ofNullable(commentData);
  }

  /**
   * Lists all comments of an article.
   *
   * <p>When a current user is given, a single batch lookup of all comment author ids ({@code
   * followingAuthors}) is performed and the {@code following} flag of each matching author profile
   * is set in place.
   *
   * @param articleId the id of the article
   * @param user the current user, or {@code null} for anonymous access
   * @return the comments of the article, possibly empty; never {@code null}
   */
  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (comments.size() > 0 && user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    return comments;
  }

  /**
   * Lists the comments of an article using cursor pagination.
   *
   * <p>The read service is asked for {@code page.getLimit() + 1} comments. If that extra record
   * comes back it is removed and used only to signal that another page exists. When paging
   * backwards ({@code !page.isNext()}) the list is reversed to restore the expected ordering. When
   * a current user is given, the {@code following} flag of each author profile is set in place via
   * a single batch {@code followingAuthors} lookup (performed before the extra record is dropped).
   *
   * @param articleId the id of the article
   * @param user the current user, or {@code null} for anonymous access
   * @param page cursor, direction and limit of the requested page
   * @return a pager holding the comments of the requested page, the paging direction and whether a
   *     further page exists in that direction; never {@code null}
   */
  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    if (user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }
}
