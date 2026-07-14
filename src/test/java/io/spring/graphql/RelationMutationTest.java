package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RelationMutationTest extends GraphQLTestBase {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;
  private User current;
  private User target;
  private final ProfileData profileData = new ProfileData("2", "bob", "bio", "image", true);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    current = new User("a@test.com", "alice", "123", "", "");
    target = new User("b@test.com", "bob", "123", "", "");
  }

  @Test
  public void should_follow_user() {
    setAuthenticatedUser(current);
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
    when(profileQueryService.findByUsername("bob", current)).thenReturn(Optional.of(profileData));

    ProfilePayload payload = relationMutation.follow("bob");
    assertEquals("bob", payload.getProfile().getUsername());
    verify(userRepository).saveRelation(new FollowRelation(current.getId(), target.getId()));
  }

  @Test
  public void should_throw_when_follow_unauthenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> relationMutation.follow("bob"));
  }

  @Test
  public void should_throw_when_follow_target_missing() {
    setAuthenticatedUser(current);
    when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("bob"));
  }

  @Test
  public void should_unfollow_user() {
    setAuthenticatedUser(current);
    FollowRelation relation = new FollowRelation(current.getId(), target.getId());
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(current.getId(), target.getId()))
        .thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername("bob", current)).thenReturn(Optional.of(profileData));

    ProfilePayload payload = relationMutation.unfollow("bob");
    assertEquals("bob", payload.getProfile().getUsername());
    verify(userRepository).removeRelation(relation);
  }

  @Test
  public void should_throw_when_unfollow_relation_missing() {
    setAuthenticatedUser(current);
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(current.getId(), target.getId())).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("bob"));
  }
}
