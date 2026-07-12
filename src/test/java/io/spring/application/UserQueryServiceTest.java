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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTest {

  @Mock private UserReadService userReadService;

  private UserQueryService userQueryService;

  @BeforeEach
  public void setUp() {
    userQueryService = new UserQueryService(userReadService);
  }

  @Test
  public void should_return_user_data_when_found() {
    UserData userData = new UserData("123", "a@test.com", "a", "bio", "image");
    when(userReadService.findById("123")).thenReturn(userData);

    Optional<UserData> optional = userQueryService.findById("123");

    assertTrue(optional.isPresent());
    assertEquals(userData, optional.get());
  }

  @Test
  public void should_return_empty_when_not_found() {
    when(userReadService.findById("404")).thenReturn(null);

    Optional<UserData> optional = userQueryService.findById("404");

    assertFalse(optional.isPresent());
  }
}
