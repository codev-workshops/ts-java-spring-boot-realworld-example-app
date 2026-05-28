package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;

  private ProfileDatafetcher profileDatafetcher;
  private User user;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("test@test.com", "testuser", "pass", "bio", "image.jpg");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() throws Exception {
    SecurityContextHolder.clearContext();
    closeable.close();
  }

  @Test
  void should_get_user_profile() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image.jpg", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
  }

  @Test
  void should_get_article_author() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "id",
            "test-slug",
            "Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            new ProfileData(user.getId(), "testuser", "bio", "image.jpg", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image.jpg", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    Comment comment = Comment.newBuilder().id("c1").body("body").build();
    when(dfe.getSource()).thenReturn(comment);

    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "c1",
            "body",
            "article-id",
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image.jpg", false));
    Map<String, CommentData> map = new HashMap<>();
    map.put("c1", commentData);
    when(dfe.getLocalContext()).thenReturn(map);

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image.jpg", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("testuser");

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image.jpg", true);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);
    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("testuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
  }
}
