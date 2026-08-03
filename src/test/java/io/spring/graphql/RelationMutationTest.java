package io.spring.graphql;

import static io.spring.graphql.GraphQLTestFixtures.anonymous;
import static io.spring.graphql.GraphQLTestFixtures.authenticate;
import static io.spring.graphql.GraphQLTestFixtures.clearAuthentication;
import static io.spring.graphql.GraphQLTestFixtures.profileData;
import static io.spring.graphql.GraphQLTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation mutation;
  private User currentUser;
  private User target;

  @BeforeEach
  void setUp() {
    mutation = new RelationMutation(userRepository, profileQueryService);
    currentUser = user("jack");
    target = user("jill");
    authenticate(currentUser);
  }

  @AfterEach
  void tearDown() {
    clearAuthentication();
  }

  @Test
  void followSavesRelationAndReturnsProfile() {
    when(userRepository.findByUsername("jill")).thenReturn(Optional.of(target));
    when(profileQueryService.findByUsername("jill", currentUser))
        .thenReturn(Optional.of(profileData("jill")));

    ProfilePayload payload = mutation.follow("jill");

    verify(userRepository).saveRelation(any(FollowRelation.class));
    assertThat(payload.getProfile().getUsername()).isEqualTo("jill");
  }

  @Test
  void followThrowsWhenAnonymous() {
    anonymous();

    assertThatThrownBy(() -> mutation.follow("jill")).isInstanceOf(AuthenticationException.class);
  }

  @Test
  void followThrowsWhenTargetMissing() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.follow("ghost"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void unfollowRemovesRelation() {
    FollowRelation relation = new FollowRelation(currentUser.getId(), target.getId());
    when(userRepository.findByUsername("jill")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(currentUser.getId(), target.getId()))
        .thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername("jill", currentUser))
        .thenReturn(Optional.of(profileData("jill")));

    ProfilePayload payload = mutation.unfollow("jill");

    verify(userRepository).removeRelation(relation);
    assertThat(payload.getProfile().getUsername()).isEqualTo("jill");
  }

  @Test
  void unfollowThrowsWhenRelationMissing() {
    when(userRepository.findByUsername("jill")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(currentUser.getId(), target.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.unfollow("jill"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void unfollowThrowsWhenTargetMissing() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.unfollow("ghost"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void unfollowThrowsWhenAnonymous() {
    anonymous();

    assertThatThrownBy(() -> mutation.unfollow("jill")).isInstanceOf(AuthenticationException.class);
  }
}
