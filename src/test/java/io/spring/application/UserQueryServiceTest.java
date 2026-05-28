package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
  void setUp() {
    MockitoAnnotations.openMocks(this);
    userQueryService = new UserQueryService(userReadService);
  }

  @Test
  void should_find_user_by_id() {
    UserData userData = new UserData("id", "test@test.com", "testuser", "bio", "image.jpg");
    when(userReadService.findById("id")).thenReturn(userData);

    Optional<UserData> result = userQueryService.findById("id");
    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
  }

  @Test
  void should_return_empty_when_user_not_found() {
    when(userReadService.findById("unknown")).thenReturn(null);

    Optional<UserData> result = userQueryService.findById("unknown");
    assertFalse(result.isPresent());
  }
}
