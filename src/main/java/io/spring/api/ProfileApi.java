package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for public user profiles and follow relationships.
 *
 * <p>Handles requests under the base path {@code profiles/{username}}: reading a profile and
 * following/unfollowing the profile's user.
 */
@RestController
@RequestMapping(path = "profiles/{username}")
@AllArgsConstructor
public class ProfileApi {
  private ProfileQueryService profileQueryService;
  private UserRepository userRepository;

  /**
   * Handles {@code GET /profiles/{username}} and returns a user's public profile.
   *
   * @param username the username of the profile to fetch
   * @param user the currently authenticated user, or {@code null} for anonymous requests; when
   *     present the {@code following} flag reflects whether this user follows the profile
   * @return {@code 200 OK} with a body of the form {@code {"profile": ProfileData}}
   * @throws ResourceNotFoundException if no user with the given username exists (mapped to {@code
   *     404 Not Found})
   */
  @GetMapping
  public ResponseEntity getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return profileQueryService
        .findByUsername(username, user)
        .map(this::profileResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Handles {@code POST /profiles/{username}/follow} and makes the current user follow the target
   * user.
   *
   * @param username the username of the user to follow
   * @param user the currently authenticated user who starts following
   * @return {@code 200 OK} with a body of the form {@code {"profile": ProfileData}} with {@code
   *     following} set to {@code true}
   * @throws ResourceNotFoundException if no user with the given username exists (mapped to {@code
   *     404 Not Found})
   */
  @PostMapping(path = "follow")
  public ResponseEntity follow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    return userRepository
        .findByUsername(username)
        .map(
            target -> {
              FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
              userRepository.saveRelation(followRelation);
              return profileResponse(profileQueryService.findByUsername(username, user).get());
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Handles {@code DELETE /profiles/{username}/follow} and makes the current user stop following
   * the target user.
   *
   * @param username the username of the user to unfollow
   * @param user the currently authenticated user who stops following
   * @return {@code 200 OK} with a body of the form {@code {"profile": ProfileData}} with {@code
   *     following} set to {@code false}
   * @throws ResourceNotFoundException if no user with the given username exists, or the current
   *     user was not following that user (mapped to {@code 404 Not Found})
   */
  @DeleteMapping(path = "follow")
  public ResponseEntity unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    Optional<User> userOptional = userRepository.findByUsername(username);
    if (userOptional.isPresent()) {
      User target = userOptional.get();
      return userRepository
          .findRelation(user.getId(), target.getId())
          .map(
              relation -> {
                userRepository.removeRelation(relation);
                return profileResponse(profileQueryService.findByUsername(username, user).get());
              })
          .orElseThrow(ResourceNotFoundException::new);
    } else {
      throw new ResourceNotFoundException();
    }
  }

  private ResponseEntity profileResponse(ProfileData profile) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profile", profile);
          }
        });
  }
}
