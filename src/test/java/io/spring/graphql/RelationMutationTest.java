package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelationMutationTest extends GraphQLTestBase {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;
  @InjectMocks private RelationMutation relationMutation;

  private final User target = new User("target@jacob.com", "target", "123", "bio", "image");
  private final ProfileData profileData = new ProfileData("id", "target", "bio", "image", true);

  @Test
  void should_follow_user() {
    authenticate(CURRENT_USER);
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    when(profileQueryService.findByUsername("target", CURRENT_USER))
        .thenReturn(Optional.of(profileData));

    ProfilePayload payload = relationMutation.follow("target");

    assertThat(payload.getProfile().getUsername()).isEqualTo("target");
    assertThat(payload.getProfile().getFollowing()).isTrue();
    verify(userRepository).saveRelation(new FollowRelation(CURRENT_USER.getId(), target.getId()));
  }

  @Test
  void should_throw_when_follow_target_missing() {
    authenticate(CURRENT_USER);
    when(userRepository.findByUsername("target")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> relationMutation.follow("target"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throw_when_follow_without_authentication() {
    anonymous();
    assertThatThrownBy(() -> relationMutation.follow("target"))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void should_unfollow_user() {
    authenticate(CURRENT_USER);
    FollowRelation relation = new FollowRelation(CURRENT_USER.getId(), target.getId());
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(CURRENT_USER.getId(), target.getId()))
        .thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername("target", CURRENT_USER))
        .thenReturn(Optional.of(profileData));

    ProfilePayload payload = relationMutation.unfollow("target");

    assertThat(payload.getProfile().getUsername()).isEqualTo("target");
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void should_throw_when_unfollow_relation_missing() {
    authenticate(CURRENT_USER);
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(CURRENT_USER.getId(), target.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> relationMutation.unfollow("target"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throw_when_unfollow_target_missing() {
    authenticate(CURRENT_USER);
    when(userRepository.findByUsername("target")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> relationMutation.unfollow("target"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throw_when_unfollow_without_authentication() {
    anonymous();
    assertThatThrownBy(() -> relationMutation.unfollow("target"))
        .isInstanceOf(AuthenticationException.class);
  }
}
