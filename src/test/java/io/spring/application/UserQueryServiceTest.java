package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserQueryServiceTest {

  @Mock private UserReadService userReadService;
  private UserQueryService userQueryService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userQueryService = new UserQueryService(userReadService);
  }

  @Test
  public void should_return_user_data_when_found() {
    UserData userData = new UserData("1", "a@test.com", "alice", "bio", "image");
    when(userReadService.findById("1")).thenReturn(userData);
    Optional<UserData> result = userQueryService.findById("1");
    assertTrue(result.isPresent());
    assertEquals("alice", result.get().getUsername());
  }

  @Test
  public void should_return_empty_when_not_found() {
    when(userReadService.findById("missing")).thenReturn(null);
    assertFalse(userQueryService.findById("missing").isPresent());
  }
}
