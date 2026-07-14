package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Collections;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMutationTest extends GraphQLTestBase {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userMutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("a@test.com", "alice", "encoded", "", "");
  }

  @Test
  public void should_create_user() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("a@test.com").username("alice").password("123").build();
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_return_error_data_on_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("a@test.com").username("alice").password("123").build();
    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(new ConstraintViolationException(Collections.emptySet()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result.getData());
  }

  @Test
  public void should_login_success() {
    when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("123", "encoded")).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("123", "a@test.com");
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_throw_when_login_password_mismatch() {
    when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", "encoded")).thenReturn(false);
    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "a@test.com"));
  }

  @Test
  public void should_throw_when_login_user_missing() {
    when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("123", "missing@test.com"));
  }

  @Test
  public void should_update_user_when_authenticated() {
    setAuthenticatedUser(user);
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("newname")
            .bio("bio")
            .password("pass")
            .image("image")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any(UpdateUserCommand.class));
  }

  @Test
  public void should_return_null_update_when_anonymous() {
    setAnonymous();
    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@test.com").build();
    assertNull(userMutation.updateUser(input));
  }
}
