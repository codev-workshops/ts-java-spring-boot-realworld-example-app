package io.spring.graphql;

import static io.spring.graphql.GraphQLTestFixtures.articleData;
import static io.spring.graphql.GraphQLTestFixtures.authenticate;
import static io.spring.graphql.GraphQLTestFixtures.clearAuthentication;
import static io.spring.graphql.GraphQLTestFixtures.commentData;
import static io.spring.graphql.GraphQLTestFixtures.profileData;
import static io.spring.graphql.GraphQLTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dfe;

  private ProfileDatafetcher datafetcher;
  private io.spring.core.user.User currentUser;

  @BeforeEach
  void setUp() {
    datafetcher = new ProfileDatafetcher(profileQueryService);
    currentUser = user("jack");
    authenticate(currentUser);
  }

  @AfterEach
  void tearDown() {
    clearAuthentication();
  }

  @Test
  void getUserProfileUsesLocalContextUser() {
    when(dfe.<io.spring.core.user.User>getLocalContext()).thenReturn(currentUser);
    when(profileQueryService.findByUsername("jack", currentUser))
        .thenReturn(Optional.of(profileData("jack")));

    Profile profile = datafetcher.getUserProfile(dfe);

    assertThat(profile.getUsername()).isEqualTo("jack");
    assertThat(profile.getFollowing()).isFalse();
  }

  @Test
  void getUserProfileThrowsWhenProfileMissing() {
    when(dfe.<io.spring.core.user.User>getLocalContext()).thenReturn(currentUser);
    when(profileQueryService.findByUsername("jack", currentUser)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> datafetcher.getUserProfile(dfe))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getAuthorResolvesArticleAuthor() {
    ArticleData article = articleData("a1", "slug-one");
    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug-one", article);
    when(dfe.<Map<String, ArticleData>>getLocalContext()).thenReturn(map);
    when(dfe.<Article>getSource()).thenReturn(Article.newBuilder().slug("slug-one").build());
    when(profileQueryService.findByUsername("author", currentUser))
        .thenReturn(Optional.of(profileData("author")));

    Profile profile = datafetcher.getAuthor(dfe);

    assertThat(profile.getUsername()).isEqualTo("author");
  }

  @Test
  void getCommentAuthorResolvesCommentAuthor() {
    CommentData comment = commentData("c1", "a1");
    Map<String, CommentData> map = new HashMap<>();
    map.put("c1", comment);
    when(dfe.<Map<String, CommentData>>getLocalContext()).thenReturn(map);
    when(dfe.<Comment>getSource()).thenReturn(Comment.newBuilder().id("c1").build());
    when(profileQueryService.findByUsername("author", currentUser))
        .thenReturn(Optional.of(profileData("author")));

    Profile profile = datafetcher.getCommentAuthor(dfe);

    assertThat(profile.getUsername()).isEqualTo("author");
  }

  @Test
  void queryProfileReadsUsernameFromArguments() {
    when(dfe.<String>getArgument("username")).thenReturn("jill");
    when(profileQueryService.findByUsername("jill", currentUser))
        .thenReturn(Optional.of(profileData("jill")));

    ProfilePayload payload = datafetcher.queryProfile("ignored", dfe);

    assertThat(payload.getProfile().getUsername()).isEqualTo("jill");
  }
}
