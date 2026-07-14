package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userService = new UserService(userRepository, "default-image.png", passwordEncoder);
  }

  @Test
  public void should_encode_password_and_save_on_create() {
    when(passwordEncoder.encode("123")).thenReturn("encoded");
    RegisterParam registerParam = new RegisterParam("new@test.com", "newuser", "123");

    User user = userService.createUser(registerParam);

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("encoded", user.getPassword());
    assertEquals("default-image.png", user.getImage());
    assertEquals("", user.getBio());
    verify(userRepository).save(user);
  }

  @Test
  public void should_apply_update_command_and_save() {
    User target = new User("old@test.com", "old", "pass", "oldbio", "oldimage");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("updated@test.com")
            .username("updated")
            .bio("updatedbio")
            .build();

    userService.updateUser(new UpdateUserCommand(target, param));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();
    assertEquals("updated@test.com", saved.getEmail());
    assertEquals("updated", saved.getUsername());
    assertEquals("updatedbio", saved.getBio());
    assertEquals("oldimage", saved.getImage());
  }

  @Test
  public void should_create_user_with_any_password_encoder_stub() {
    when(passwordEncoder.encode(anyString())).thenReturn("x");
    User user = userService.createUser(new RegisterParam("a@b.com", "ab", "p"));
    verify(userRepository).save(any(User.class));
    assertEquals("x", user.getPassword());
  }
}
