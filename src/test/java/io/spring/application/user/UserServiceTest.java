package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(passwordEncoder.encode(any())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
    userService = new UserService(userRepository, "https://default-image.jpg", passwordEncoder);
  }

  @Test
  void should_create_user() {
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");

    User user = userService.createUser(param);
    assertNotNull(user);
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("encoded_password", user.getPassword());
    assertEquals("https://default-image.jpg", user.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void should_update_user() {
    User user = new User("old@test.com", "olduser", "oldpass", "old bio", "old.jpg");

    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .bio("new bio")
            .image("new.jpg")
            .password("")
            .build();

    UpdateUserCommand command = new UpdateUserCommand(user, param);
    userService.updateUser(command);

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("new bio", user.getBio());
    assertEquals("new.jpg", user.getImage());
    verify(userRepository).save(user);
  }
}
