package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProfileDatafetcherTest extends GraphQLTestBase {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dfe;

  private ProfileDatafetcher profileDatafetcher;
  private final ProfileData profileData = new ProfileData("1", "alice", "bio", "image", true);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    setAnonymous();
  }

  @Test
  public void should_query_profile_by_argument() {
    when(dfe.getArgument("username")).thenReturn("alice");
    when(profileQueryService.findByUsername("alice", null)).thenReturn(Optional.of(profileData));

    ProfilePayload payload = profileDatafetcher.queryProfile("alice", dfe);
    Profile profile = payload.getProfile();
    assertEquals("alice", profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertEquals("image", profile.getImage());
    assertTrue(profile.getFollowing());
  }

  @Test
  public void should_throw_when_profile_not_found() {
    when(dfe.getArgument("username")).thenReturn("ghost");
    when(profileQueryService.findByUsername("ghost", null)).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> profileDatafetcher.queryProfile("ghost", dfe));
  }

  @Test
  public void should_get_user_profile_from_local_context() {
    User user = new User("a@test.com", "alice", "123", "", "");
    when(dfe.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername("alice", null)).thenReturn(Optional.of(profileData));

    Profile profile = profileDatafetcher.getUserProfile(dfe);
    assertEquals("alice", profile.getUsername());
  }

  @Test
  public void should_get_author_from_article_source() {
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
            profileData);
    Map<String, ArticleData> map = Collections.singletonMap("slug-1", articleData);
    io.spring.graphql.types.Article article =
        io.spring.graphql.types.Article.newBuilder().slug("slug-1").build();
    when(dfe.getLocalContext()).thenReturn(map);
    when(dfe.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername("alice", null)).thenReturn(Optional.of(profileData));

    Profile profile = profileDatafetcher.getAuthor(dfe);
    assertEquals("alice", profile.getUsername());
  }

  @Test
  public void should_get_comment_author_from_source() {
    CommentData commentData =
        new CommentData("cid", "body", "aid", new DateTime(), new DateTime(), profileData);
    Map<String, CommentData> map = Collections.singletonMap("cid", commentData);
    io.spring.graphql.types.Comment comment =
        io.spring.graphql.types.Comment.newBuilder().id("cid").build();
    when(dfe.getLocalContext()).thenReturn(map);
    when(dfe.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername("alice", null)).thenReturn(Optional.of(profileData));

    Profile profile = profileDatafetcher.getCommentAuthor(dfe);
    assertEquals("alice", profile.getUsername());
  }
}
