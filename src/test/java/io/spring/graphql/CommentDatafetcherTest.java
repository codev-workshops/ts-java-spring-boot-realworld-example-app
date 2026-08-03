package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.Collections;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentDatafetcherTest extends GraphQLTestBase {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;
  @InjectMocks private CommentDatafetcher commentDatafetcher;

  private final ProfileData profileData = new ProfileData("id", "johnjacob", "bio", "image", false);
  private final CommentData commentData =
      new CommentData("cid", "body", "aid", new DateTime(), new DateTime(), profileData);
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
          Collections.emptyList(),
          profileData);

  @Test
  void should_build_comment_from_local_context() {
    when(dfe.<CommentData>getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertThat(result.getData().getId()).isEqualTo("cid");
    assertThat(result.getData().getBody()).isEqualTo("body");
    assertThat((Map<String, Object>) result.getLocalContext()).containsKey("cid");
  }

  @Test
  void should_list_article_comments_forward() {
    anonymous();
    when(dfe.<Article>getSource()).thenReturn(Article.newBuilder().slug("a-slug").build());
    when(dfe.<Map<String, ArticleData>>getLocalContext())
        .thenReturn(Collections.singletonMap("a-slug", articleData));
    when(commentQueryService.findByArticleIdWithCursor(
            eq("aid"), any(), any(CursorPageParameter.class)))
        .thenReturn(
            new CursorPager<>(Collections.singletonList(commentData), Direction.NEXT, false));

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
    assertThat(result.getData().getEdges().get(0).getNode().getId()).isEqualTo("cid");
    assertThat(result.getData().getPageInfo().isHasNextPage()).isFalse();
  }

  @Test
  void should_list_article_comments_backward() {
    anonymous();
    when(dfe.<Article>getSource()).thenReturn(Article.newBuilder().slug("a-slug").build());
    when(dfe.<Map<String, ArticleData>>getLocalContext())
        .thenReturn(Collections.singletonMap("a-slug", articleData));
    when(commentQueryService.findByArticleIdWithCursor(
            eq("aid"), any(), any(CursorPageParameter.class)))
        .thenReturn(
            new CursorPager<>(Collections.singletonList(commentData), Direction.PREV, true));

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(
            null, null, 10, String.valueOf(new DateTime().getMillis()), dfe);

    assertThat(result.getData().getPageInfo().isHasPreviousPage()).isTrue();
  }

  @Test
  void should_reject_when_neither_first_nor_last_given() {
    assertThatThrownBy(() -> commentDatafetcher.articleComments(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_return_empty_page_info_when_no_comments() {
    anonymous();
    when(dfe.<Article>getSource()).thenReturn(Article.newBuilder().slug("a-slug").build());
    when(dfe.<Map<String, ArticleData>>getLocalContext())
        .thenReturn(Collections.singletonMap("a-slug", articleData));
    when(commentQueryService.findByArticleIdWithCursor(
            eq("aid"), any(), any(CursorPageParameter.class)))
        .thenReturn(new CursorPager<>(Collections.emptyList(), Direction.NEXT, false));

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).isEmpty();
    assertThat(result.getData().getPageInfo().getStartCursor()).isNull();
    assertThat(result.getData().getPageInfo().getEndCursor()).isNull();
  }
}
