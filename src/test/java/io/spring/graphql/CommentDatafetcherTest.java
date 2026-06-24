package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;

  private CommentDatafetcher fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new CommentDatafetcher(commentQueryService);
  }

  private CommentData createCommentData(String id) {
    return new CommentData(
        id,
        "body",
        "articleId",
        new DateTime(),
        new DateTime(),
        new ProfileData("userId", "author", "bio", "image", false));
  }

  @Test
  void testGetComment() {
    CommentData commentData = createCommentData("c1");
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = fetcher.getComment(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("c1", result.getData().getId());
    assertEquals("body", result.getData().getBody());
  }

  @Test
  void testArticleCommentsWithFirst() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Article article = Article.newBuilder().slug("slug1").build();
      when(dfe.getSource()).thenReturn(article);

      ArticleData articleData =
          new ArticleData(
              "artId",
              "slug1",
              "title",
              "desc",
              "body",
              false,
              0,
              new DateTime(),
              new DateTime(),
              Collections.emptyList(),
              new ProfileData("userId", "author", "bio", "image", false));
      Map<String, ArticleData> map = new HashMap<>();
      map.put("slug1", articleData);
      when(dfe.getLocalContext()).thenReturn(map);

      CommentData commentData = createCommentData("c1");
      CursorPager<CommentData> pager =
          new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, true);
      when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

      DataFetcherResult<CommentsConnection> result =
          fetcher.articleComments(10, null, null, null, dfe);

      assertNotNull(result);
      assertNotNull(result.getData());
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void testArticleCommentsWithLast() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Article article = Article.newBuilder().slug("slug1").build();
      when(dfe.getSource()).thenReturn(article);

      ArticleData articleData =
          new ArticleData(
              "artId",
              "slug1",
              "title",
              "desc",
              "body",
              false,
              0,
              new DateTime(),
              new DateTime(),
              Collections.emptyList(),
              new ProfileData("userId", "author", "bio", "image", false));
      Map<String, ArticleData> map = new HashMap<>();
      map.put("slug1", articleData);
      when(dfe.getLocalContext()).thenReturn(map);

      CommentData commentData = createCommentData("c1");
      CursorPager<CommentData> pager =
          new CursorPager<>(Arrays.asList(commentData), Direction.PREV, true);
      when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

      DataFetcherResult<CommentsConnection> result =
          fetcher.articleComments(null, null, 10, null, dfe);

      assertNotNull(result);
      assertNotNull(result.getData());
    }
  }

  @Test
  void testArticleCommentsNoFirstOrLast() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void testArticleCommentsWithEmptyResults() {
    try (MockedStatic<SecurityUtil> secUtil = mockStatic(SecurityUtil.class)) {
      secUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Article article = Article.newBuilder().slug("slug1").build();
      when(dfe.getSource()).thenReturn(article);

      ArticleData articleData =
          new ArticleData(
              "artId",
              "slug1",
              "title",
              "desc",
              "body",
              false,
              0,
              new DateTime(),
              new DateTime(),
              Collections.emptyList(),
              new ProfileData("userId", "author", "bio", "image", false));
      Map<String, ArticleData> map = new HashMap<>();
      map.put("slug1", articleData);
      when(dfe.getLocalContext()).thenReturn(map);

      CursorPager<CommentData> pager =
          new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
      when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

      DataFetcherResult<CommentsConnection> result =
          fetcher.articleComments(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(0, result.getData().getEdges().size());
    }
  }
}
