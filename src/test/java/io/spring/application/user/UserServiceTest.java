package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService =
        new UserService(userRepository, "https://default.image/avatar.jpg", passwordEncoder);
  }

  @Test
  void createUser_saves_user_with_encoded_password() {
    when(passwordEncoder.encode("rawpass")).thenReturn("encodedpass");
    RegisterParam registerParam = new RegisterParam("test@email.com", "testuser", "rawpass");

    User result = userService.createUser(registerParam);

    assertNotNull(result);
    assertEquals("test@email.com", result.getEmail());
    assertEquals("testuser", result.getUsername());
    assertEquals("encodedpass", result.getPassword());
    assertEquals("https://default.image/avatar.jpg", result.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void updateUser_delegates_to_user_update_and_saves() {
    User user = new User("old@email.com", "oldname", "oldpass", "oldbio", "oldimage");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@email.com")
            .username("newname")
            .password("")
            .bio("newbio")
            .image("")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    userService.updateUser(command);

    assertEquals("new@email.com", user.getEmail());
    assertEquals("newname", user.getUsername());
    assertEquals("newbio", user.getBio());
    verify(userRepository).save(user);
  }
}
