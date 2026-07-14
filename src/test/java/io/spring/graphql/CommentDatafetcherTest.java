package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.Collections;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CommentDatafetcherTest extends GraphQLTestBase {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;

  private CommentDatafetcher commentDatafetcher;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    setAnonymous();
  }

  private CommentData commentData() {
    return new CommentData(
        "cid",
        "body",
        "aid",
        new DateTime(),
        new DateTime(),
        new ProfileData("1", "alice", "", "", false));
  }

  @Test
  public void should_get_comment_from_local_context() {
    when(dfe.getLocalContext()).thenReturn(commentData());
    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);
    assertEquals("cid", result.getData().getId());
    assertEquals("body", result.getData().getBody());
  }

  @Test
  public void should_get_article_comments_with_first() {
    io.spring.graphql.types.Article article =
        io.spring.graphql.types.Article.newBuilder().slug("slug-1").build();
    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "aid",
            "slug-1",
            "t",
            "d",
            "b",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            new ProfileData("1", "alice", "", "", false));
    Map<String, ArticleData> map = Collections.singletonMap("slug-1", articleData);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.singletonList(commentData()), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("aid"), any(), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_article_comments_with_last() {
    io.spring.graphql.types.Article article =
        io.spring.graphql.types.Article.newBuilder().slug("slug-1").build();
    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "aid",
            "slug-1",
            "t",
            "d",
            "b",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            new ProfileData("1", "alice", "", "", false));
    Map<String, ArticleData> map = Collections.singletonMap("slug-1", articleData);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("aid"), any(), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_no_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }
}
