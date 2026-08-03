package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileDatafetcherTest extends GraphQLTestBase {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dfe;
  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  private final ProfileData profileData = new ProfileData("id", "johnjacob", "bio", "image", true);

  @Test
  void should_get_user_profile_from_local_context() {
    anonymous();
    when(dfe.<io.spring.core.user.User>getLocalContext()).thenReturn(CURRENT_USER);
    when(profileQueryService.findByUsername(eq("johnjacob"), any()))
        .thenReturn(Optional.of(profileData));

    Profile profile = profileDatafetcher.getUserProfile(dfe);

    assertThat(profile.getUsername()).isEqualTo("johnjacob");
    assertThat(profile.getBio()).isEqualTo("bio");
    assertThat(profile.getImage()).isEqualTo("image");
    assertThat(profile.getFollowing()).isTrue();
  }

  @Test
  void should_get_article_author_using_current_user() {
    authenticate(CURRENT_USER);
    ArticleData articleData =
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
    when(dfe.<Map<String, ArticleData>>getLocalContext())
        .thenReturn(Collections.singletonMap("a-slug", articleData));
    when(dfe.<Article>getSource()).thenReturn(Article.newBuilder().slug("a-slug").build());
    when(profileQueryService.findByUsername("johnjacob", CURRENT_USER))
        .thenReturn(Optional.of(profileData));

    assertThat(profileDatafetcher.getAuthor(dfe).getUsername()).isEqualTo("johnjacob");
  }

  @Test
  void should_get_comment_author() {
    anonymous();
    CommentData commentData =
        new CommentData("cid", "body", "aid", new DateTime(), new DateTime(), profileData);
    when(dfe.<Comment>getSource()).thenReturn(Comment.newBuilder().id("cid").build());
    when(dfe.<Map<String, CommentData>>getLocalContext())
        .thenReturn(Collections.singletonMap("cid", commentData));
    when(profileQueryService.findByUsername(eq("johnjacob"), any()))
        .thenReturn(Optional.of(profileData));

    assertThat(profileDatafetcher.getCommentAuthor(dfe).getUsername()).isEqualTo("johnjacob");
  }

  @Test
  void should_query_profile_by_argument() {
    anonymous();
    when(dfe.<String>getArgument("username")).thenReturn("johnjacob");
    when(profileQueryService.findByUsername(eq("johnjacob"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload payload = profileDatafetcher.queryProfile("johnjacob", dfe);

    assertThat(payload.getProfile().getUsername()).isEqualTo("johnjacob");
  }

  @Test
  void should_throw_when_profile_not_found() {
    anonymous();
    when(dfe.<String>getArgument("username")).thenReturn("missing");
    when(profileQueryService.findByUsername(eq("missing"), any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> profileDatafetcher.queryProfile("missing", dfe))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
