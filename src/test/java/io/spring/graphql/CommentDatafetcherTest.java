package io.spring.graphql;

import static io.spring.graphql.GraphQLTestFixtures.authenticate;
import static io.spring.graphql.GraphQLTestFixtures.clearAuthentication;
import static io.spring.graphql.GraphQLTestFixtures.commentData;
import static io.spring.graphql.GraphQLTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;

  private CommentDatafetcher datafetcher;
  private io.spring.core.user.User currentUser;

  @BeforeEach
  void setUp() {
    datafetcher = new CommentDatafetcher(commentQueryService);
    currentUser = user("jack");
    authenticate(currentUser);
  }

  @AfterEach
  void tearDown() {
    clearAuthentication();
  }

  private void stubArticleContext(ArticleData articleData) {
    Map<String, ArticleData> map = new HashMap<>();
    map.put(articleData.getSlug(), articleData);
    when(dfe.getSource()).thenReturn(Article.newBuilder().slug(articleData.getSlug()).build());
    when(dfe.<Map<String, ArticleData>>getLocalContext()).thenReturn(map);
  }

  @Test
  void getCommentBuildsResultFromLocalContext() {
    CommentData comment = commentData("c1", "a1");
    when(dfe.<CommentData>getLocalContext()).thenReturn(comment);

    DataFetcherResult<Comment> result = datafetcher.getComment(dfe);

    assertThat(result.getData().getId()).isEqualTo("c1");
    assertThat(result.getData().getBody()).isEqualTo("comment body");
  }

  @Test
  void articleCommentsWithFirstReturnsConnection() {
    ArticleData articleData = GraphQLTestFixtures.articleData("a1", "slug-one");
    stubArticleContext(articleData);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), eq(currentUser), any()))
        .thenReturn(
            new CursorPager<>(Arrays.asList(commentData("c1", "a1")), Direction.NEXT, false));

    DataFetcherResult<CommentsConnection> result =
        datafetcher.articleComments(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).hasSize(1);
    assertThat(result.getData().getEdges().get(0).getNode().getId()).isEqualTo("c1");
  }

  @Test
  void articleCommentsWithLastReturnsConnection() {
    ArticleData articleData = GraphQLTestFixtures.articleData("a1", "slug-one");
    stubArticleContext(articleData);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), eq(currentUser), any()))
        .thenReturn(
            new CursorPager<>(Arrays.asList(commentData("c1", "a1")), Direction.PREV, true));

    DataFetcherResult<CommentsConnection> result =
        datafetcher.articleComments(null, null, 10, null, dfe);

    assertThat(result.getData().getPageInfo().isHasPreviousPage()).isTrue();
  }

  @Test
  void articleCommentsWithEmptyPagerHasNullCursors() {
    ArticleData articleData = GraphQLTestFixtures.articleData("a1", "slug-one");
    stubArticleContext(articleData);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), eq(currentUser), any()))
        .thenReturn(new CursorPager<CommentData>(Collections.emptyList(), Direction.NEXT, false));

    DataFetcherResult<CommentsConnection> result =
        datafetcher.articleComments(10, null, null, null, dfe);

    assertThat(result.getData().getEdges()).isEmpty();
    assertThat(result.getData().getPageInfo().getStartCursor()).isNull();
  }

  @Test
  void articleCommentsWithoutFirstAndLastThrows() {
    assertThatThrownBy(() -> datafetcher.articleComments(null, null, null, null, dfe))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
