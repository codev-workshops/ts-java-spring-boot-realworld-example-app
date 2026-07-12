package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private static final String DEFAULT_IMAGE =
      "https://static.productionready.io/images/smiley-cyrus.jpg";

  private UserService userService;

  @BeforeEach
  public void setUp() {
    userService = new UserService(userRepository, DEFAULT_IMAGE, passwordEncoder);
  }

  @Test
  public void should_encode_password_apply_default_image_and_save() {
    RegisterParam param = new RegisterParam("a@test.com", "a", "plain");
    when(passwordEncoder.encode("plain")).thenReturn("encoded");

    User user = userService.createUser(param);

    assertEquals("a@test.com", user.getEmail());
    assertEquals("a", user.getUsername());
    assertEquals("encoded", user.getPassword());
    assertEquals(DEFAULT_IMAGE, user.getImage());
    assertEquals("", user.getBio());

    verify(passwordEncoder).encode("plain");
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(user.getId(), captor.getValue().getId());
  }

  @Test
  public void should_update_user_fields_and_save() {
    User user = new User("old@test.com", "old", "123", "old bio", "old image");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newname")
            .bio("new bio")
            .image("new image")
            .build();

    userService.updateUser(new UpdateUserCommand(user, param));

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newname", user.getUsername());
    assertEquals("new bio", user.getBio());
    assertEquals("new image", user.getImage());
    verify(userRepository).save(user);
  }
}
