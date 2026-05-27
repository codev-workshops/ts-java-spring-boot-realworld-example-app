package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;
  private User user;
  private User target;

  @BeforeEach
  void setUp() {
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    user = new User("test@email.com", "testuser", "pass", "", "");
    target = new User("target@email.com", "targetuser", "pass", "", "");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(u, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void follow_success() {
    setAuthenticated(user);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    ProfileData profileData = new ProfileData(target.getId(), "targetuser", "", "", true);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertNotNull(result);
    assertEquals("targetuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void follow_throws_when_not_authenticated() {
    setAnonymous();

    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  void follow_throws_when_target_not_found() {
    setAuthenticated(user);
    when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("missing"));
  }

  @Test
  void unfollow_success() {
    setAuthenticated(user);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    FollowRelation relation = new FollowRelation(user.getId(), target.getId());
    when(userRepository.findRelation(user.getId(), target.getId()))
        .thenReturn(Optional.of(relation));
    ProfileData profileData = new ProfileData(target.getId(), "targetuser", "", "", false);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertNotNull(result);
    assertFalse(result.getProfile().getFollowing());
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void unfollow_throws_when_not_authenticated() {
    setAnonymous();

    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  void unfollow_throws_when_relation_not_found() {
    setAuthenticated(user);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(user.getId(), target.getId())).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
