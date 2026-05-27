package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTest {

  @Mock private UserReadService userReadService;

  private UserQueryService userQueryService;

  @BeforeEach
  void setUp() {
    userQueryService = new UserQueryService(userReadService);
  }

  @Test
  void should_return_user_data_when_found() {
    UserData userData = new UserData("id1", "test@example.com", "testuser", "bio", "image");
    when(userReadService.findById("id1")).thenReturn(userData);

    Optional<UserData> result = userQueryService.findById("id1");

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
  }

  @Test
  void should_return_empty_when_not_found() {
    when(userReadService.findById("missing")).thenReturn(null);

    Optional<UserData> result = userQueryService.findById("missing");

    assertFalse(result.isPresent());
  }
}
