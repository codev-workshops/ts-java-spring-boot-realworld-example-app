package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;

  private CommentDatafetcher commentDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@email.com", "testuser", "pass", "", "");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private CommentData buildCommentData(String id) {
    return new CommentData(
        id,
        "comment body",
        "a1",
        new DateTime(),
        new DateTime(),
        new ProfileData("u1", "author", "", "", false));
  }

  @Test
  void getComment_returns_comment_from_local_context() {
    CommentData commentData = buildCommentData("c1");

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertEquals("c1", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
  }

  @Test
  void articleComments_with_first_param() {
    ArticleData articleData =
        new ArticleData(
            "a1",
            "slug1",
            "Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Collections.emptyList(),
            new ProfileData("uid", "testuser", "", "", false));
    CommentData cd = buildCommentData("c1");
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(cd), CursorPager.Direction.NEXT, true);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), any(), any())).thenReturn(pager);

    Article article = Article.newBuilder().slug("slug1").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug1", articleData);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(5, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  void articleComments_with_last_param() {
    ArticleData articleData =
        new ArticleData(
            "a1",
            "slug1",
            "Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Collections.emptyList(),
            new ProfileData("uid", "testuser", "", "", false));
    CursorPager<CommentData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), any(), any())).thenReturn(pager);

    Article article = Article.newBuilder().slug("slug1").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug1", articleData);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 5, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  void articleComments_throws_when_first_and_last_are_null() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }
}
