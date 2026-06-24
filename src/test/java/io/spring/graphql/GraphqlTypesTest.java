package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticleEdge;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentEdge;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.CommentsConnection;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.ErrorItem;
import io.spring.graphql.types.PageInfo;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import io.spring.graphql.types.UpdateArticleInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.User;
import io.spring.graphql.types.UserPayload;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphqlTypesTest {

  // ========== Article ==========
  @Test
  void testArticleEqualsAndHashCode() {
    Article a1 = Article.newBuilder().slug("s1").title("t1").body("b1").build();
    Article a2 = Article.newBuilder().slug("s1").title("t1").body("b1").build();
    Article a3 = Article.newBuilder().slug("s2").title("t2").body("b2").build();
    assertEquals(a1, a1);
    assertEquals(a1, a2);
    assertNotEquals(a1, a3);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
    assertEquals(a1.hashCode(), a2.hashCode());
    assertNotNull(a1.toString());
  }

  @Test
  void testArticleFieldByFieldEquality() {
    Article base =
        Article.newBuilder()
            .slug("s")
            .title("t")
            .body("b")
            .description("d")
            .createdAt("c")
            .updatedAt("u")
            .favorited(false)
            .favoritesCount(0)
            .tagList(Arrays.asList("tag1"))
            .build();

    // differ by each field
    assertNotEquals(
        base,
        Article.newBuilder()
            .slug("x")
            .title("t")
            .body("b")
            .description("d")
            .createdAt("c")
            .updatedAt("u")
            .favorited(false)
            .favoritesCount(0)
            .tagList(Arrays.asList("tag1"))
            .build());
    assertNotEquals(
        base,
        Article.newBuilder()
            .slug("s")
            .title("x")
            .body("b")
            .description("d")
            .createdAt("c")
            .updatedAt("u")
            .favorited(false)
            .favoritesCount(0)
            .tagList(Arrays.asList("tag1"))
            .build());
    assertNotEquals(
        base,
        Article.newBuilder()
            .slug("s")
            .title("t")
            .body("x")
            .description("d")
            .createdAt("c")
            .updatedAt("u")
            .favorited(false)
            .favoritesCount(0)
            .tagList(Arrays.asList("tag1"))
            .build());
    assertNotEquals(
        base,
        Article.newBuilder()
            .slug("s")
            .title("t")
            .body("b")
            .description("x")
            .createdAt("c")
            .updatedAt("u")
            .favorited(false)
            .favoritesCount(0)
            .tagList(Arrays.asList("tag1"))
            .build());
    assertNotEquals(
        base,
        Article.newBuilder()
            .slug("s")
            .title("t")
            .body("b")
            .description("d")
            .createdAt("x")
            .updatedAt("u")
            .favorited(false)
            .favoritesCount(0)
            .tagList(Arrays.asList("tag1"))
            .build());
    assertNotEquals(
        base,
        Article.newBuilder()
            .slug("s")
            .title("t")
            .body("b")
            .description("d")
            .createdAt("c")
            .updatedAt("x")
            .favorited(false)
            .favoritesCount(0)
            .tagList(Arrays.asList("tag1"))
            .build());
    assertNotEquals(
        base,
        Article.newBuilder()
            .slug("s")
            .title("t")
            .body("b")
            .description("d")
            .createdAt("c")
            .updatedAt("u")
            .favorited(true)
            .favoritesCount(0)
            .tagList(Arrays.asList("tag1"))
            .build());
    assertNotEquals(
        base,
        Article.newBuilder()
            .slug("s")
            .title("t")
            .body("b")
            .description("d")
            .createdAt("c")
            .updatedAt("u")
            .favorited(false)
            .favoritesCount(99)
            .tagList(Arrays.asList("tag1"))
            .build());
    assertNotEquals(
        base,
        Article.newBuilder()
            .slug("s")
            .title("t")
            .body("b")
            .description("d")
            .createdAt("c")
            .updatedAt("u")
            .favorited(false)
            .favoritesCount(0)
            .tagList(Arrays.asList("other"))
            .build());
  }

  @Test
  void testArticleAuthorField() {
    Profile p1 = Profile.newBuilder().username("u1").build();
    Profile p2 = Profile.newBuilder().username("u2").build();
    Article a1 = Article.newBuilder().slug("s").author(p1).build();
    Article a2 = Article.newBuilder().slug("s").author(p1).build();
    Article a3 = Article.newBuilder().slug("s").author(p2).build();
    assertEquals(a1, a2);
    assertNotEquals(a1, a3);
  }

  @Test
  void testArticleCommentsField() {
    CommentsConnection cc1 = CommentsConnection.newBuilder().build();
    Article a1 = Article.newBuilder().slug("s").comments(cc1).build();
    Article a2 = Article.newBuilder().slug("s").comments(cc1).build();
    Article a3 = Article.newBuilder().slug("s").build();
    assertEquals(a1, a2);
    assertNotEquals(a1, a3);
  }

  // ========== ArticleEdge ==========
  @Test
  void testArticleEdgeEqualsAndHashCode() {
    Article article = Article.newBuilder().slug("s1").build();
    ArticleEdge e1 = ArticleEdge.newBuilder().cursor("c1").node(article).build();
    ArticleEdge e2 = ArticleEdge.newBuilder().cursor("c1").node(article).build();
    ArticleEdge e3 = ArticleEdge.newBuilder().cursor("c2").node(article).build();
    assertEquals(e1, e1);
    assertEquals(e1, e2);
    assertNotEquals(e1, e3);
    assertNotEquals(e1, null);
    assertNotEquals(e1, "string");
    assertEquals(e1.hashCode(), e2.hashCode());
    assertNotNull(e1.toString());
  }

  @Test
  void testArticleEdgeFieldByField() {
    Article a1 = Article.newBuilder().slug("s1").build();
    Article a2 = Article.newBuilder().slug("s2").build();
    ArticleEdge base = ArticleEdge.newBuilder().cursor("c").node(a1).build();
    assertNotEquals(base, ArticleEdge.newBuilder().cursor("x").node(a1).build());
    assertNotEquals(base, ArticleEdge.newBuilder().cursor("c").node(a2).build());
  }

  // ========== ArticlePayload ==========
  @Test
  void testArticlePayloadEqualsAndHashCode() {
    Article article = Article.newBuilder().slug("s1").build();
    ArticlePayload p1 = ArticlePayload.newBuilder().article(article).build();
    ArticlePayload p2 = ArticlePayload.newBuilder().article(article).build();
    ArticlePayload p3 = ArticlePayload.newBuilder().build();
    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertNotEquals(p1, null);
    assertNotEquals(p1, "string");
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
  }

  // ========== ArticlesConnection ==========
  @Test
  void testArticlesConnectionEqualsAndHashCode() {
    ArticleEdge edge = ArticleEdge.newBuilder().cursor("c1").build();
    graphql.relay.PageInfo pi = new graphql.relay.DefaultPageInfo(null, null, false, true);
    ArticlesConnection c1 =
        ArticlesConnection.newBuilder().edges(Arrays.asList(edge)).pageInfo(pi).build();
    ArticlesConnection c2 =
        ArticlesConnection.newBuilder().edges(Arrays.asList(edge)).pageInfo(pi).build();
    ArticlesConnection c3 = ArticlesConnection.newBuilder().build();
    assertEquals(c1, c1);
    assertEquals(c1, c2);
    assertNotEquals(c1, c3);
    assertNotEquals(c1, null);
    assertNotEquals(c1, "string");
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotNull(c1.toString());
  }

  @Test
  void testArticlesConnectionFieldByField() {
    ArticleEdge edge1 = ArticleEdge.newBuilder().cursor("c1").build();
    ArticleEdge edge2 = ArticleEdge.newBuilder().cursor("c2").build();
    graphql.relay.PageInfo pi1 = new graphql.relay.DefaultPageInfo(null, null, false, true);
    graphql.relay.PageInfo pi2 = new graphql.relay.DefaultPageInfo(null, null, true, false);
    ArticlesConnection base =
        ArticlesConnection.newBuilder().edges(Arrays.asList(edge1)).pageInfo(pi1).build();
    assertNotEquals(
        base, ArticlesConnection.newBuilder().edges(Arrays.asList(edge2)).pageInfo(pi1).build());
    assertNotEquals(
        base, ArticlesConnection.newBuilder().edges(Arrays.asList(edge1)).pageInfo(pi2).build());
  }

  // ========== Comment ==========
  @Test
  void testCommentEqualsAndHashCode() {
    Comment c1 = Comment.newBuilder().id("id1").body("b1").createdAt("t1").updatedAt("t1").build();
    Comment c2 = Comment.newBuilder().id("id1").body("b1").createdAt("t1").updatedAt("t1").build();
    Comment c3 = Comment.newBuilder().id("id2").body("b2").createdAt("t2").updatedAt("t2").build();
    assertEquals(c1, c1);
    assertEquals(c1, c2);
    assertNotEquals(c1, c3);
    assertNotEquals(c1, null);
    assertNotEquals(c1, "string");
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotNull(c1.toString());
  }

  @Test
  void testCommentFieldByField() {
    Comment base = Comment.newBuilder().id("id").body("b").createdAt("c").updatedAt("u").build();
    assertNotEquals(
        base, Comment.newBuilder().id("x").body("b").createdAt("c").updatedAt("u").build());
    assertNotEquals(
        base, Comment.newBuilder().id("id").body("x").createdAt("c").updatedAt("u").build());
    assertNotEquals(
        base, Comment.newBuilder().id("id").body("b").createdAt("x").updatedAt("u").build());
    assertNotEquals(
        base, Comment.newBuilder().id("id").body("b").createdAt("c").updatedAt("x").build());
  }

  @Test
  void testCommentAuthorAndArticleFields() {
    Profile p1 = Profile.newBuilder().username("u1").build();
    Profile p2 = Profile.newBuilder().username("u2").build();
    Article ar1 = Article.newBuilder().slug("s1").build();
    Article ar2 = Article.newBuilder().slug("s2").build();
    Comment c1 = Comment.newBuilder().id("id").author(p1).article(ar1).build();
    Comment c2 = Comment.newBuilder().id("id").author(p1).article(ar1).build();
    Comment c3 = Comment.newBuilder().id("id").author(p2).article(ar1).build();
    Comment c4 = Comment.newBuilder().id("id").author(p1).article(ar2).build();
    assertEquals(c1, c2);
    assertNotEquals(c1, c3);
    assertNotEquals(c1, c4);
  }

  // ========== CommentEdge ==========
  @Test
  void testCommentEdgeEqualsAndHashCode() {
    Comment comment = Comment.newBuilder().id("id1").build();
    CommentEdge e1 = CommentEdge.newBuilder().cursor("c1").node(comment).build();
    CommentEdge e2 = CommentEdge.newBuilder().cursor("c1").node(comment).build();
    CommentEdge e3 = CommentEdge.newBuilder().cursor("c2").node(comment).build();
    assertEquals(e1, e1);
    assertEquals(e1, e2);
    assertNotEquals(e1, e3);
    assertNotEquals(e1, null);
    assertNotEquals(e1, "string");
    assertEquals(e1.hashCode(), e2.hashCode());
    assertNotNull(e1.toString());
  }

  @Test
  void testCommentEdgeFieldByField() {
    Comment cm1 = Comment.newBuilder().id("id1").build();
    Comment cm2 = Comment.newBuilder().id("id2").build();
    CommentEdge base = CommentEdge.newBuilder().cursor("c").node(cm1).build();
    assertNotEquals(base, CommentEdge.newBuilder().cursor("x").node(cm1).build());
    assertNotEquals(base, CommentEdge.newBuilder().cursor("c").node(cm2).build());
  }

  // ========== CommentPayload ==========
  @Test
  void testCommentPayloadEqualsAndHashCode() {
    Comment comment = Comment.newBuilder().id("id1").build();
    CommentPayload p1 = CommentPayload.newBuilder().comment(comment).build();
    CommentPayload p2 = CommentPayload.newBuilder().comment(comment).build();
    CommentPayload p3 = CommentPayload.newBuilder().build();
    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertNotEquals(p1, null);
    assertNotEquals(p1, "string");
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
  }

  // ========== CommentsConnection ==========
  @Test
  void testCommentsConnectionEqualsAndHashCode() {
    CommentEdge edge = CommentEdge.newBuilder().cursor("c1").build();
    graphql.relay.PageInfo pi = new graphql.relay.DefaultPageInfo(null, null, false, true);
    CommentsConnection c1 =
        CommentsConnection.newBuilder().edges(Arrays.asList(edge)).pageInfo(pi).build();
    CommentsConnection c2 =
        CommentsConnection.newBuilder().edges(Arrays.asList(edge)).pageInfo(pi).build();
    CommentsConnection c3 = CommentsConnection.newBuilder().build();
    assertEquals(c1, c1);
    assertEquals(c1, c2);
    assertNotEquals(c1, c3);
    assertNotEquals(c1, null);
    assertNotEquals(c1, "string");
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotNull(c1.toString());
  }

  @Test
  void testCommentsConnectionFieldByField() {
    CommentEdge edge1 = CommentEdge.newBuilder().cursor("c1").build();
    CommentEdge edge2 = CommentEdge.newBuilder().cursor("c2").build();
    graphql.relay.PageInfo pi1 = new graphql.relay.DefaultPageInfo(null, null, false, true);
    graphql.relay.PageInfo pi2 = new graphql.relay.DefaultPageInfo(null, null, true, false);
    CommentsConnection base =
        CommentsConnection.newBuilder().edges(Arrays.asList(edge1)).pageInfo(pi1).build();
    assertNotEquals(
        base, CommentsConnection.newBuilder().edges(Arrays.asList(edge2)).pageInfo(pi1).build());
    assertNotEquals(
        base, CommentsConnection.newBuilder().edges(Arrays.asList(edge1)).pageInfo(pi2).build());
  }

  // ========== CreateArticleInput ==========
  @Test
  void testCreateArticleInputEqualsAndHashCode() {
    CreateArticleInput i1 =
        CreateArticleInput.newBuilder()
            .title("t1")
            .body("b1")
            .description("d1")
            .tagList(Arrays.asList("tag1"))
            .build();
    CreateArticleInput i2 =
        CreateArticleInput.newBuilder()
            .title("t1")
            .body("b1")
            .description("d1")
            .tagList(Arrays.asList("tag1"))
            .build();
    CreateArticleInput i3 =
        CreateArticleInput.newBuilder()
            .title("t2")
            .body("b2")
            .description("d2")
            .tagList(Arrays.asList("tag2"))
            .build();
    assertEquals(i1, i1);
    assertEquals(i1, i2);
    assertNotEquals(i1, i3);
    assertNotEquals(i1, null);
    assertNotEquals(i1, "string");
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotNull(i1.toString());
  }

  @Test
  void testCreateArticleInputFieldByField() {
    CreateArticleInput base =
        CreateArticleInput.newBuilder()
            .title("t")
            .body("b")
            .description("d")
            .tagList(Arrays.asList("tag"))
            .build();
    assertNotEquals(
        base,
        CreateArticleInput.newBuilder()
            .title("x")
            .body("b")
            .description("d")
            .tagList(Arrays.asList("tag"))
            .build());
    assertNotEquals(
        base,
        CreateArticleInput.newBuilder()
            .title("t")
            .body("x")
            .description("d")
            .tagList(Arrays.asList("tag"))
            .build());
    assertNotEquals(
        base,
        CreateArticleInput.newBuilder()
            .title("t")
            .body("b")
            .description("x")
            .tagList(Arrays.asList("tag"))
            .build());
    assertNotEquals(
        base,
        CreateArticleInput.newBuilder()
            .title("t")
            .body("b")
            .description("d")
            .tagList(Arrays.asList("x"))
            .build());
  }

  // ========== CreateUserInput ==========
  @Test
  void testCreateUserInputEqualsAndHashCode() {
    CreateUserInput i1 =
        CreateUserInput.newBuilder().email("e1").username("u1").password("p1").build();
    CreateUserInput i2 =
        CreateUserInput.newBuilder().email("e1").username("u1").password("p1").build();
    CreateUserInput i3 =
        CreateUserInput.newBuilder().email("e2").username("u2").password("p2").build();
    assertEquals(i1, i1);
    assertEquals(i1, i2);
    assertNotEquals(i1, i3);
    assertNotEquals(i1, null);
    assertNotEquals(i1, "string");
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotNull(i1.toString());
  }

  @Test
  void testCreateUserInputFieldByField() {
    CreateUserInput base =
        CreateUserInput.newBuilder().email("e").username("u").password("p").build();
    assertNotEquals(
        base, CreateUserInput.newBuilder().email("x").username("u").password("p").build());
    assertNotEquals(
        base, CreateUserInput.newBuilder().email("e").username("x").password("p").build());
    assertNotEquals(
        base, CreateUserInput.newBuilder().email("e").username("u").password("x").build());
  }

  // ========== DeletionStatus ==========
  @Test
  void testDeletionStatusEqualsAndHashCode() {
    DeletionStatus d1 = DeletionStatus.newBuilder().success(true).build();
    DeletionStatus d2 = DeletionStatus.newBuilder().success(true).build();
    DeletionStatus d3 = DeletionStatus.newBuilder().success(false).build();
    assertEquals(d1, d1);
    assertEquals(d1, d2);
    assertNotEquals(d1, d3);
    assertNotEquals(d1, null);
    assertNotEquals(d1, "string");
    assertEquals(d1.hashCode(), d2.hashCode());
    assertNotNull(d1.toString());
  }

  // ========== Error ==========
  @Test
  void testErrorEqualsAndHashCode() {
    ErrorItem item = ErrorItem.newBuilder().key("k1").value(Arrays.asList("v1")).build();
    io.spring.graphql.types.Error e1 =
        io.spring.graphql.types.Error.newBuilder()
            .message("m1")
            .errors(Arrays.asList(item))
            .build();
    io.spring.graphql.types.Error e2 =
        io.spring.graphql.types.Error.newBuilder()
            .message("m1")
            .errors(Arrays.asList(item))
            .build();
    io.spring.graphql.types.Error e3 =
        io.spring.graphql.types.Error.newBuilder()
            .message("m2")
            .errors(Collections.emptyList())
            .build();
    assertEquals(e1, e1);
    assertEquals(e1, e2);
    assertNotEquals(e1, e3);
    assertNotEquals(e1, null);
    assertNotEquals(e1, "string");
    assertEquals(e1.hashCode(), e2.hashCode());
    assertNotNull(e1.toString());
  }

  @Test
  void testErrorFieldByField() {
    ErrorItem item = ErrorItem.newBuilder().key("k").value(Arrays.asList("v")).build();
    io.spring.graphql.types.Error base =
        io.spring.graphql.types.Error.newBuilder().message("m").errors(Arrays.asList(item)).build();
    assertNotEquals(
        base,
        io.spring.graphql.types.Error.newBuilder()
            .message("x")
            .errors(Arrays.asList(item))
            .build());
    assertNotEquals(
        base,
        io.spring.graphql.types.Error.newBuilder()
            .message("m")
            .errors(Collections.emptyList())
            .build());
  }

  // ========== ErrorItem ==========
  @Test
  void testErrorItemEqualsAndHashCode() {
    ErrorItem i1 = ErrorItem.newBuilder().key("k1").value(Arrays.asList("v1")).build();
    ErrorItem i2 = ErrorItem.newBuilder().key("k1").value(Arrays.asList("v1")).build();
    ErrorItem i3 = ErrorItem.newBuilder().key("k2").value(Arrays.asList("v2")).build();
    assertEquals(i1, i1);
    assertEquals(i1, i2);
    assertNotEquals(i1, i3);
    assertNotEquals(i1, null);
    assertNotEquals(i1, "string");
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotNull(i1.toString());
  }

  @Test
  void testErrorItemFieldByField() {
    ErrorItem base = ErrorItem.newBuilder().key("k").value(Arrays.asList("v")).build();
    assertNotEquals(base, ErrorItem.newBuilder().key("x").value(Arrays.asList("v")).build());
    assertNotEquals(base, ErrorItem.newBuilder().key("k").value(Arrays.asList("x")).build());
  }

  // ========== PageInfo ==========
  @Test
  void testPageInfoEqualsAndHashCode() {
    PageInfo p1 =
        PageInfo.newBuilder()
            .endCursor("e1")
            .startCursor("s1")
            .hasNextPage(true)
            .hasPreviousPage(false)
            .build();
    PageInfo p2 =
        PageInfo.newBuilder()
            .endCursor("e1")
            .startCursor("s1")
            .hasNextPage(true)
            .hasPreviousPage(false)
            .build();
    PageInfo p3 =
        PageInfo.newBuilder()
            .endCursor("e2")
            .startCursor("s2")
            .hasNextPage(false)
            .hasPreviousPage(true)
            .build();
    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertNotEquals(p1, null);
    assertNotEquals(p1, "string");
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
  }

  @Test
  void testPageInfoFieldByField() {
    PageInfo base =
        PageInfo.newBuilder()
            .endCursor("e")
            .startCursor("s")
            .hasNextPage(true)
            .hasPreviousPage(false)
            .build();
    assertNotEquals(
        base,
        PageInfo.newBuilder()
            .endCursor("x")
            .startCursor("s")
            .hasNextPage(true)
            .hasPreviousPage(false)
            .build());
    assertNotEquals(
        base,
        PageInfo.newBuilder()
            .endCursor("e")
            .startCursor("x")
            .hasNextPage(true)
            .hasPreviousPage(false)
            .build());
    assertNotEquals(
        base,
        PageInfo.newBuilder()
            .endCursor("e")
            .startCursor("s")
            .hasNextPage(false)
            .hasPreviousPage(false)
            .build());
    assertNotEquals(
        base,
        PageInfo.newBuilder()
            .endCursor("e")
            .startCursor("s")
            .hasNextPage(true)
            .hasPreviousPage(true)
            .build());
  }

  // ========== Profile ==========
  @Test
  void testProfileEqualsAndHashCode() {
    Profile p1 = Profile.newBuilder().username("u1").bio("b1").image("i1").following(true).build();
    Profile p2 = Profile.newBuilder().username("u1").bio("b1").image("i1").following(true).build();
    Profile p3 = Profile.newBuilder().username("u2").bio("b2").image("i2").following(false).build();
    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertNotEquals(p1, null);
    assertNotEquals(p1, "string");
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
  }

  @Test
  void testProfileFieldByField() {
    Profile base = Profile.newBuilder().username("u").bio("b").image("i").following(false).build();
    assertNotEquals(
        base, Profile.newBuilder().username("x").bio("b").image("i").following(false).build());
    assertNotEquals(
        base, Profile.newBuilder().username("u").bio("x").image("i").following(false).build());
    assertNotEquals(
        base, Profile.newBuilder().username("u").bio("b").image("x").following(false).build());
    assertNotEquals(
        base, Profile.newBuilder().username("u").bio("b").image("i").following(true).build());
  }

  @Test
  void testProfileArticlesFavoritesAndFeedFields() {
    ArticlesConnection ac1 = ArticlesConnection.newBuilder().build();
    ArticlesConnection ac2 =
        ArticlesConnection.newBuilder()
            .edges(Arrays.asList(ArticleEdge.newBuilder().cursor("c").build()))
            .build();
    Profile p1 = Profile.newBuilder().username("u").articles(ac1).favorites(ac1).feed(ac1).build();
    Profile p2 = Profile.newBuilder().username("u").articles(ac1).favorites(ac1).feed(ac1).build();
    Profile p3 = Profile.newBuilder().username("u").articles(ac2).favorites(ac1).feed(ac1).build();
    Profile p4 = Profile.newBuilder().username("u").articles(ac1).favorites(ac2).feed(ac1).build();
    Profile p5 = Profile.newBuilder().username("u").articles(ac1).favorites(ac1).feed(ac2).build();
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertNotEquals(p1, p4);
    assertNotEquals(p1, p5);
  }

  // ========== ProfilePayload ==========
  @Test
  void testProfilePayloadEqualsAndHashCode() {
    Profile profile = Profile.newBuilder().username("u1").build();
    ProfilePayload p1 = ProfilePayload.newBuilder().profile(profile).build();
    ProfilePayload p2 = ProfilePayload.newBuilder().profile(profile).build();
    ProfilePayload p3 = ProfilePayload.newBuilder().build();
    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertNotEquals(p1, null);
    assertNotEquals(p1, "string");
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
  }

  // ========== UpdateArticleInput ==========
  @Test
  void testUpdateArticleInputEqualsAndHashCode() {
    UpdateArticleInput i1 =
        UpdateArticleInput.newBuilder().title("t1").body("b1").description("d1").build();
    UpdateArticleInput i2 =
        UpdateArticleInput.newBuilder().title("t1").body("b1").description("d1").build();
    UpdateArticleInput i3 =
        UpdateArticleInput.newBuilder().title("t2").body("b2").description("d2").build();
    assertEquals(i1, i1);
    assertEquals(i1, i2);
    assertNotEquals(i1, i3);
    assertNotEquals(i1, null);
    assertNotEquals(i1, "string");
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotNull(i1.toString());
  }

  @Test
  void testUpdateArticleInputFieldByField() {
    UpdateArticleInput base =
        UpdateArticleInput.newBuilder().title("t").body("b").description("d").build();
    assertNotEquals(
        base, UpdateArticleInput.newBuilder().title("x").body("b").description("d").build());
    assertNotEquals(
        base, UpdateArticleInput.newBuilder().title("t").body("x").description("d").build());
    assertNotEquals(
        base, UpdateArticleInput.newBuilder().title("t").body("b").description("x").build());
  }

  // ========== UpdateUserInput ==========
  @Test
  void testUpdateUserInputEqualsAndHashCode() {
    UpdateUserInput i1 =
        UpdateUserInput.newBuilder()
            .email("e1")
            .username("u1")
            .password("p1")
            .image("i1")
            .bio("b1")
            .build();
    UpdateUserInput i2 =
        UpdateUserInput.newBuilder()
            .email("e1")
            .username("u1")
            .password("p1")
            .image("i1")
            .bio("b1")
            .build();
    UpdateUserInput i3 =
        UpdateUserInput.newBuilder()
            .email("e2")
            .username("u2")
            .password("p2")
            .image("i2")
            .bio("b2")
            .build();
    assertEquals(i1, i1);
    assertEquals(i1, i2);
    assertNotEquals(i1, i3);
    assertNotEquals(i1, null);
    assertNotEquals(i1, "string");
    assertEquals(i1.hashCode(), i2.hashCode());
    assertNotNull(i1.toString());
  }

  @Test
  void testUpdateUserInputFieldByField() {
    UpdateUserInput base =
        UpdateUserInput.newBuilder()
            .email("e")
            .username("u")
            .password("p")
            .image("i")
            .bio("b")
            .build();
    assertNotEquals(
        base,
        UpdateUserInput.newBuilder()
            .email("x")
            .username("u")
            .password("p")
            .image("i")
            .bio("b")
            .build());
    assertNotEquals(
        base,
        UpdateUserInput.newBuilder()
            .email("e")
            .username("x")
            .password("p")
            .image("i")
            .bio("b")
            .build());
    assertNotEquals(
        base,
        UpdateUserInput.newBuilder()
            .email("e")
            .username("u")
            .password("x")
            .image("i")
            .bio("b")
            .build());
    assertNotEquals(
        base,
        UpdateUserInput.newBuilder()
            .email("e")
            .username("u")
            .password("p")
            .image("x")
            .bio("b")
            .build());
    assertNotEquals(
        base,
        UpdateUserInput.newBuilder()
            .email("e")
            .username("u")
            .password("p")
            .image("i")
            .bio("x")
            .build());
  }

  // ========== User ==========
  @Test
  void testUserEqualsAndHashCode() {
    User u1 = User.newBuilder().email("e1").username("u1").token("t1").build();
    User u2 = User.newBuilder().email("e1").username("u1").token("t1").build();
    User u3 = User.newBuilder().email("e2").username("u2").token("t2").build();
    assertEquals(u1, u1);
    assertEquals(u1, u2);
    assertNotEquals(u1, u3);
    assertNotEquals(u1, null);
    assertNotEquals(u1, "string");
    assertEquals(u1.hashCode(), u2.hashCode());
    assertNotNull(u1.toString());
  }

  @Test
  void testUserFieldByField() {
    User base = User.newBuilder().email("e").username("u").token("t").build();
    assertNotEquals(base, User.newBuilder().email("x").username("u").token("t").build());
    assertNotEquals(base, User.newBuilder().email("e").username("x").token("t").build());
    assertNotEquals(base, User.newBuilder().email("e").username("u").token("x").build());
  }

  @Test
  void testUserProfileField() {
    Profile p1 = Profile.newBuilder().username("u1").build();
    Profile p2 = Profile.newBuilder().username("u2").build();
    User u1 = User.newBuilder().email("e").profile(p1).build();
    User u2 = User.newBuilder().email("e").profile(p1).build();
    User u3 = User.newBuilder().email("e").profile(p2).build();
    assertEquals(u1, u2);
    assertNotEquals(u1, u3);
  }

  // ========== UserPayload ==========
  @Test
  void testUserPayloadEqualsAndHashCode() {
    User user = User.newBuilder().email("e1").build();
    UserPayload p1 = UserPayload.newBuilder().user(user).build();
    UserPayload p2 = UserPayload.newBuilder().user(user).build();
    UserPayload p3 = UserPayload.newBuilder().build();
    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertNotEquals(p1, null);
    assertNotEquals(p1, "string");
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
  }

  // ========== Setters ==========
  @Test
  void testArticleSetters() {
    Article a = new Article();
    a.setSlug("s");
    a.setTitle("t");
    a.setBody("b");
    a.setDescription("d");
    a.setCreatedAt("c");
    a.setUpdatedAt("u");
    a.setFavorited(true);
    a.setFavoritesCount(5);
    a.setTagList(Arrays.asList("tag"));
    a.setAuthor(Profile.newBuilder().build());
    a.setComments(CommentsConnection.newBuilder().build());
    assertEquals("s", a.getSlug());
    assertEquals("t", a.getTitle());
    assertEquals("b", a.getBody());
    assertEquals("d", a.getDescription());
    assertEquals("c", a.getCreatedAt());
    assertEquals("u", a.getUpdatedAt());
    assertTrue(a.getFavorited());
    assertEquals(5, a.getFavoritesCount());
    assertEquals(Arrays.asList("tag"), a.getTagList());
    assertNotNull(a.getAuthor());
    assertNotNull(a.getComments());
  }

  @Test
  void testCommentSetters() {
    Comment c = new Comment();
    c.setId("id");
    c.setBody("b");
    c.setCreatedAt("c");
    c.setUpdatedAt("u");
    c.setAuthor(Profile.newBuilder().build());
    c.setArticle(Article.newBuilder().build());
    assertEquals("id", c.getId());
    assertEquals("b", c.getBody());
    assertEquals("c", c.getCreatedAt());
    assertEquals("u", c.getUpdatedAt());
    assertNotNull(c.getAuthor());
    assertNotNull(c.getArticle());
  }

  @Test
  void testProfileSetters() {
    Profile p = new Profile();
    p.setUsername("u");
    p.setBio("b");
    p.setImage("i");
    p.setFollowing(true);
    p.setArticles(ArticlesConnection.newBuilder().build());
    p.setFavorites(ArticlesConnection.newBuilder().build());
    p.setFeed(ArticlesConnection.newBuilder().build());
    assertEquals("u", p.getUsername());
    assertEquals("b", p.getBio());
    assertEquals("i", p.getImage());
    assertTrue(p.getFollowing());
    assertNotNull(p.getArticles());
    assertNotNull(p.getFavorites());
    assertNotNull(p.getFeed());
  }

  @Test
  void testPageInfoSetters() {
    PageInfo pi = new PageInfo();
    pi.setEndCursor("e");
    pi.setStartCursor("s");
    pi.setHasNextPage(true);
    pi.setHasPreviousPage(true);
    assertEquals("e", pi.getEndCursor());
    assertEquals("s", pi.getStartCursor());
    assertTrue(pi.getHasNextPage());
    assertTrue(pi.getHasPreviousPage());
  }

  @Test
  void testEdgeSetters() {
    ArticleEdge ae = new ArticleEdge();
    ae.setCursor("c");
    ae.setNode(Article.newBuilder().build());
    assertEquals("c", ae.getCursor());
    assertNotNull(ae.getNode());

    CommentEdge ce = new CommentEdge();
    ce.setCursor("c");
    ce.setNode(Comment.newBuilder().build());
    assertEquals("c", ce.getCursor());
    assertNotNull(ce.getNode());
  }

  @Test
  void testConnectionSetters() {
    ArticlesConnection ac = new ArticlesConnection();
    ac.setEdges(Collections.emptyList());
    ac.setPageInfo(new graphql.relay.DefaultPageInfo(null, null, false, false));
    assertNotNull(ac.getEdges());
    assertNotNull(ac.getPageInfo());

    CommentsConnection cc = new CommentsConnection();
    cc.setEdges(Collections.emptyList());
    cc.setPageInfo(new graphql.relay.DefaultPageInfo(null, null, false, false));
    assertNotNull(cc.getEdges());
    assertNotNull(cc.getPageInfo());
  }

  @Test
  void testPayloadSetters() {
    ArticlePayload ap = new ArticlePayload();
    ap.setArticle(Article.newBuilder().build());
    assertNotNull(ap.getArticle());

    CommentPayload cp = new CommentPayload();
    cp.setComment(Comment.newBuilder().build());
    assertNotNull(cp.getComment());

    ProfilePayload pp = new ProfilePayload();
    pp.setProfile(Profile.newBuilder().build());
    assertNotNull(pp.getProfile());

    UserPayload up = new UserPayload();
    up.setUser(User.newBuilder().build());
    assertNotNull(up.getUser());
  }

  @Test
  void testInputSetters() {
    CreateArticleInput cai = new CreateArticleInput();
    cai.setTitle("t");
    cai.setBody("b");
    cai.setDescription("d");
    cai.setTagList(Arrays.asList("tag"));
    assertEquals("t", cai.getTitle());
    assertEquals("b", cai.getBody());
    assertEquals("d", cai.getDescription());
    assertEquals(Arrays.asList("tag"), cai.getTagList());

    CreateUserInput cui = new CreateUserInput();
    cui.setEmail("e");
    cui.setUsername("u");
    cui.setPassword("p");
    assertEquals("e", cui.getEmail());
    assertEquals("u", cui.getUsername());
    assertEquals("p", cui.getPassword());

    UpdateArticleInput uai = new UpdateArticleInput();
    uai.setTitle("t");
    uai.setBody("b");
    uai.setDescription("d");
    assertEquals("t", uai.getTitle());
    assertEquals("b", uai.getBody());
    assertEquals("d", uai.getDescription());

    UpdateUserInput uui = new UpdateUserInput();
    uui.setEmail("e");
    uui.setUsername("u");
    uui.setPassword("p");
    uui.setImage("i");
    uui.setBio("b");
    assertEquals("e", uui.getEmail());
    assertEquals("u", uui.getUsername());
    assertEquals("p", uui.getPassword());
    assertEquals("i", uui.getImage());
    assertEquals("b", uui.getBio());
  }

  @Test
  void testDeletionStatusSetters() {
    DeletionStatus ds = new DeletionStatus();
    ds.setSuccess(true);
    assertTrue(ds.getSuccess());
  }

  @Test
  void testErrorSetters() {
    io.spring.graphql.types.Error e = new io.spring.graphql.types.Error();
    e.setMessage("m");
    e.setErrors(Collections.emptyList());
    assertEquals("m", e.getMessage());
    assertNotNull(e.getErrors());

    ErrorItem ei = new ErrorItem();
    ei.setKey("k");
    ei.setValue(Arrays.asList("v"));
    assertEquals("k", ei.getKey());
    assertEquals(Arrays.asList("v"), ei.getValue());
  }

  @Test
  void testUserSetters() {
    User u = new User();
    u.setEmail("e");
    u.setUsername("u");
    u.setToken("t");
    u.setProfile(Profile.newBuilder().build());
    assertEquals("e", u.getEmail());
    assertEquals("u", u.getUsername());
    assertEquals("t", u.getToken());
    assertNotNull(u.getProfile());
  }

  // ========== Constructor tests ==========
  @Test
  void testArticleAllArgsConstructor() {
    Profile author = Profile.newBuilder().username("u").build();
    CommentsConnection comments = CommentsConnection.newBuilder().build();
    List<String> tags = Arrays.asList("t1", "t2");
    Article a =
        new Article(
            author, "body", comments, "created", "desc", true, 5, "slug", tags, "title", "updated");
    assertEquals("slug", a.getSlug());
    assertEquals("title", a.getTitle());
    assertEquals("body", a.getBody());
    assertEquals("desc", a.getDescription());
    assertEquals("created", a.getCreatedAt());
    assertEquals("updated", a.getUpdatedAt());
    assertTrue(a.getFavorited());
    assertEquals(5, a.getFavoritesCount());
    assertEquals(tags, a.getTagList());
    assertEquals(author, a.getAuthor());
    assertEquals(comments, a.getComments());
  }

  @Test
  void testCommentAllArgsConstructor() {
    Profile author = Profile.newBuilder().username("u").build();
    Article article = Article.newBuilder().slug("s").build();
    Comment c = new Comment("id", author, article, "body", "created", "updated");
    assertEquals("id", c.getId());
    assertEquals(author, c.getAuthor());
    assertEquals(article, c.getArticle());
    assertEquals("body", c.getBody());
    assertEquals("created", c.getCreatedAt());
    assertEquals("updated", c.getUpdatedAt());
  }

  @Test
  void testProfileAllArgsConstructor() {
    ArticlesConnection ac = ArticlesConnection.newBuilder().build();
    Profile p = new Profile("user", "bio", true, "img", ac, ac, ac);
    assertEquals("user", p.getUsername());
    assertEquals("bio", p.getBio());
    assertTrue(p.getFollowing());
    assertEquals("img", p.getImage());
    assertEquals(ac, p.getArticles());
    assertEquals(ac, p.getFavorites());
    assertEquals(ac, p.getFeed());
  }
}
