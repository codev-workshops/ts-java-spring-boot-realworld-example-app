package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
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
import io.spring.graphql.types.Profile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleDatafetcherTest extends GraphQLTestBase {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;
  @InjectMocks private ArticleDatafetcher articleDatafetcher;

  private final ProfileData profileData = new ProfileData("id", "johnjacob", "bio", "image", false);
  private final ArticleData articleData =
      new ArticleData(
          "aid",
          "a-slug",
          "title",
          "desc",
          "body",
          false,
          0,
          new DateTime(),
          new DateTime(),
          Arrays.asList("java"),
          profileData);

  private CursorPager<ArticleData> onePage(Direction direction, boolean hasExtra) {
    return new CursorPager<>(Collections.singletonList(articleData), direction, hasExtra);
  }

  private String cursor() {
    return String.valueOf(new DateTime().getMillis());
  }

  @Test
  void should_get_feed_forward() {
    authenticate(CURRENT_USER);
    when(articleQueryService.findUserFeedWithCursor(
            eq(CURRENT_USER), any(CursorPageParameter.class)))
        .thenReturn(onePage(Direction.NEXT, true));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
    assertThat(result.getData().getEdges().get(0).getNode().getSlug()).isEqualTo("a-slug");
    assertThat(result.getData().getPageInfo().isHasNextPage()).isTrue();
    assertThat((Map<String, ArticleData>) result.getLocalContext()).containsKey("a-slug");
  }

  @Test
  void should_get_feed_backward() {
    anonymous();
    when(articleQueryService.findUserFeedWithCursor(isNull(), any(CursorPageParameter.class)))
        .thenReturn(onePage(Direction.PREV, true));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, cursor(), dfe);

    assertThat(result.getData().getPageInfo().isHasPreviousPage()).isTrue();
  }

  @Test
  void should_reject_feed_without_first_or_last() {
    assertThatThrownBy(() -> articleDatafetcher.getFeed(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_get_user_feed_forward() {
    User target = new User("t@t.com", "target", "123", "bio", "image");
    when(dfe.<Profile>getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    when(articleQueryService.findUserFeedWithCursor(eq(target), any(CursorPageParameter.class)))
        .thenReturn(onePage(Direction.NEXT, false));

    assertThat(articleDatafetcher.userFeed(10, null, null, null, dfe).getData().getEdges())
        .hasSize(1);
  }

  @Test
  void should_get_user_feed_backward() {
    User target = new User("t@t.com", "target", "123", "bio", "image");
    when(dfe.<Profile>getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    when(articleQueryService.findUserFeedWithCursor(eq(target), any(CursorPageParameter.class)))
        .thenReturn(onePage(Direction.PREV, false));

    assertThat(
            articleDatafetcher
                .userFeed(null, null, 10, cursor(), dfe)
                .getData()
                .getPageInfo()
                .isHasPreviousPage())
        .isFalse();
  }

  @Test
  void should_throw_when_user_feed_target_missing() {
    when(dfe.<Profile>getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(userRepository.findByUsername("target")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleDatafetcher.userFeed(10, null, null, null, dfe))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_reject_user_feed_without_first_or_last() {
    assertThatThrownBy(() -> articleDatafetcher.userFeed(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_get_user_favorites_forward() {
    anonymous();
    when(dfe.<Profile>getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("target"), any(CursorPageParameter.class), isNull()))
        .thenReturn(onePage(Direction.NEXT, false));

    assertThat(articleDatafetcher.userFavorites(10, null, null, null, dfe).getData().getEdges())
        .hasSize(1);
  }

  @Test
  void should_get_user_favorites_backward() {
    anonymous();
    when(dfe.<Profile>getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("target"), any(CursorPageParameter.class), isNull()))
        .thenReturn(onePage(Direction.PREV, false));

    assertThat(articleDatafetcher.userFavorites(null, null, 10, cursor(), dfe).getData())
        .isNotNull();
  }

  @Test
  void should_reject_user_favorites_without_first_or_last() {
    assertThatThrownBy(() -> articleDatafetcher.userFavorites(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_get_user_articles_forward() {
    anonymous();
    when(dfe.<Profile>getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq("target"), isNull(), any(CursorPageParameter.class), isNull()))
        .thenReturn(onePage(Direction.NEXT, false));

    assertThat(articleDatafetcher.userArticles(10, null, null, null, dfe).getData().getEdges())
        .hasSize(1);
  }

  @Test
  void should_get_user_articles_backward() {
    anonymous();
    when(dfe.<Profile>getSource()).thenReturn(Profile.newBuilder().username("target").build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq("target"), isNull(), any(CursorPageParameter.class), isNull()))
        .thenReturn(onePage(Direction.PREV, false));

    assertThat(articleDatafetcher.userArticles(null, null, 10, cursor(), dfe).getData())
        .isNotNull();
  }

  @Test
  void should_reject_user_articles_without_first_or_last() {
    assertThatThrownBy(() -> articleDatafetcher.userArticles(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_get_articles_forward_with_filters() {
    anonymous();
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("author"), eq("fan"), any(CursorPageParameter.class), isNull()))
        .thenReturn(onePage(Direction.NEXT, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author", "fan", "java", dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
    assertThat(result.getData().getPageInfo().getStartCursor()).isNotNull();
  }

  @Test
  void should_get_articles_backward_with_filters() {
    anonymous();
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("author"), eq("fan"), any(CursorPageParameter.class), isNull()))
        .thenReturn(onePage(Direction.PREV, false));

    assertThat(
            articleDatafetcher
                .getArticles(null, null, 10, cursor(), "author", "fan", "java", dfe)
                .getData())
        .isNotNull();
  }

  @Test
  void should_reject_articles_without_first_or_last() {
    assertThatThrownBy(
            () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_get_article_from_payload_local_context() {
    anonymous();
    io.spring.core.article.Article article =
        new io.spring.core.article.Article(
            "title", "desc", "body", Arrays.asList("java"), CURRENT_USER.getId());
    when(dfe.<io.spring.core.article.Article>getLocalContext()).thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), isNull()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result = articleDatafetcher.getArticle(dfe);

    assertThat(result.getData().getSlug()).isEqualTo("a-slug");
    assertThat(result.getData().getTagList()).containsExactly("java");
    assertThat((Map<String, Object>) result.getLocalContext()).containsKey("a-slug");
  }

  @Test
  void should_throw_when_payload_article_missing() {
    anonymous();
    io.spring.core.article.Article article =
        new io.spring.core.article.Article(
            "title", "desc", "body", Arrays.asList("java"), CURRENT_USER.getId());
    when(dfe.<io.spring.core.article.Article>getLocalContext()).thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), isNull())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleDatafetcher.getArticle(dfe))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_get_comment_article() {
    anonymous();
    CommentData commentData =
        new CommentData("cid", "body", "aid", new DateTime(), new DateTime(), profileData);
    when(dfe.<CommentData>getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("aid"), isNull())).thenReturn(Optional.of(articleData));

    assertThat(articleDatafetcher.getCommentArticle(dfe).getData().getSlug()).isEqualTo("a-slug");
  }

  @Test
  void should_throw_when_comment_article_missing() {
    anonymous();
    CommentData commentData =
        new CommentData("cid", "body", "aid", new DateTime(), new DateTime(), profileData);
    when(dfe.<CommentData>getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("aid"), isNull())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleDatafetcher.getCommentArticle(dfe))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_find_article_by_slug() {
    anonymous();
    when(articleQueryService.findBySlug(eq("a-slug"), isNull()))
        .thenReturn(Optional.of(articleData));

    assertThat(articleDatafetcher.findArticleBySlug("a-slug").getData().getTitle())
        .isEqualTo("title");
  }

  @Test
  void should_throw_when_article_slug_missing() {
    anonymous();
    when(articleQueryService.findBySlug(eq("missing"), isNull())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleDatafetcher.findArticleBySlug("missing"))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
