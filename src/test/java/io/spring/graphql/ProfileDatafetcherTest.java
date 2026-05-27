package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;

  private ProfileDatafetcher profileDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("test@email.com", "testuser", "pass", "", "");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getUserProfile_returns_profile() {
    ProfileData profileData = new ProfileData("uid", "testuser", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    Profile result = profileDatafetcher.getUserProfile(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
  }

  @Test
  void queryProfile_returns_profile_payload() {
    ProfileData profileData = new ProfileData("uid", "testuser", "bio", "image", true);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("testuser");

    ProfilePayload result = profileDatafetcher.queryProfile("testuser", dfe);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("testuser", result.getProfile().getUsername());
  }

  @Test
  void getAuthor_returns_profile_from_article_context() {
    ProfileData profileData = new ProfileData("uid", "author", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("author"), any()))
        .thenReturn(Optional.of(profileData));

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
            new ProfileData("uid", "author", "bio", "image", false));

    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug1", articleData);

    Article article = Article.newBuilder().slug("slug1").build();
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    when(dfe.getSource()).thenReturn(article);

    Profile result = profileDatafetcher.getAuthor(dfe);

    assertNotNull(result);
    assertEquals("author", result.getUsername());
  }

  @Test
  void getCommentAuthor_returns_profile_from_comment_context() {
    ProfileData profileData = new ProfileData("uid", "commentauthor", "bio", "image", false);
    when(profileQueryService.findByUsername(eq("commentauthor"), any()))
        .thenReturn(Optional.of(profileData));

    CommentData commentData =
        new CommentData(
            "c1",
            "comment body",
            "a1",
            new DateTime(),
            new DateTime(),
            new ProfileData("uid", "commentauthor", "bio", "image", false));

    Map<String, CommentData> map = new HashMap<>();
    map.put("c1", commentData);

    Comment comment = Comment.newBuilder().id("c1").build();
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    when(dfe.getSource()).thenReturn(comment);

    Profile result = profileDatafetcher.getCommentAuthor(dfe);

    assertNotNull(result);
    assertEquals("commentauthor", result.getUsername());
  }
}
