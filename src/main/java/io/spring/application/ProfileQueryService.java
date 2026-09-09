package io.spring.application;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/** Read-side application service that builds public {@link ProfileData} views of users. */
@Component
@AllArgsConstructor
public class ProfileQueryService {
  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;

  /**
   * Finds the public profile of a user by username.
   *
   * @param username the username to look up
   * @param currentUser the current user, or {@code null} for anonymous access; the {@code
   *     following} flag of the profile is {@code true} only if this user follows the target
   * @return the profile, or {@link Optional#empty()} if no user has the given username
   */
  public Optional<ProfileData> findByUsername(String username, User currentUser) {
    UserData userData = userReadService.findByUsername(username);
    if (userData == null) {
      return Optional.empty();
    } else {
      ProfileData profileData =
          new ProfileData(
              userData.getId(),
              userData.getUsername(),
              userData.getBio(),
              userData.getImage(),
              currentUser != null
                  && userRelationshipQueryService.isUserFollowing(
                      currentUser.getId(), userData.getId()));
      return Optional.of(profileData);
    }
  }
}
