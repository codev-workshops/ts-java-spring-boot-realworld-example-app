package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ProfileQueryServiceUnitTest {

  private UserReadService userReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ProfileQueryService profileQueryService;

  @BeforeEach
  public void setUp() {
    userReadService = Mockito.mock(UserReadService.class);
    userRelationshipQueryService = Mockito.mock(UserRelationshipQueryService.class);
    profileQueryService = new ProfileQueryService(userReadService, userRelationshipQueryService);
  }

  @Test
  public void testFindByUsernameReturnsEmptyWhenNotFound() {
    when(userReadService.findByUsername("nonexistent")).thenReturn(null);
    User user = new User("a@test.com", "a", "123", "", "");
    Optional<ProfileData> result = profileQueryService.findByUsername("nonexistent", user);
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindByUsernameWithNullUser() {
    UserData userData = new UserData("uid", "email@test.com", "targetUser", "bio", "img");
    when(userReadService.findByUsername("targetUser")).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("targetUser", null);
    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
    assertEquals("targetUser", result.get().getUsername());
  }

  @Test
  public void testFindByUsernameWithUserFollowing() {
    UserData userData = new UserData("uid", "email@test.com", "targetUser", "bio", "img");
    when(userReadService.findByUsername("targetUser")).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(anyString(), eq("uid"))).thenReturn(true);

    User currentUser = new User("c@test.com", "c", "123", "", "");
    Optional<ProfileData> result = profileQueryService.findByUsername("targetUser", currentUser);
    assertTrue(result.isPresent());
    assertTrue(result.get().isFollowing());
  }

  @Test
  public void testFindByUsernameWithUserNotFollowing() {
    UserData userData = new UserData("uid", "email@test.com", "targetUser", "bio", "img");
    when(userReadService.findByUsername("targetUser")).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(anyString(), eq("uid"))).thenReturn(false);

    User currentUser = new User("c@test.com", "c", "123", "", "");
    Optional<ProfileData> result = profileQueryService.findByUsername("targetUser", currentUser);
    assertTrue(result.isPresent());
    assertFalse(result.get().isFollowing());
  }
}
