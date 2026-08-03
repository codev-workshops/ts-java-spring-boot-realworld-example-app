package io.spring.graphql;

import static io.spring.graphql.GraphQLTestFixtures.anonymous;
import static io.spring.graphql.GraphQLTestFixtures.authenticate;
import static io.spring.graphql.GraphQLTestFixtures.clearAuthentication;
import static io.spring.graphql.GraphQLTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import io.spring.graphql.types.Error;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Collections;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation mutation;
  private User currentUser;

  @BeforeEach
  void setUp() {
    mutation = new UserMutation(userRepository, encryptService, userService);
    currentUser = user("jack");
  }

  @AfterEach
  void tearDown() {
    clearAuthentication();
  }

  @Test
  void createUserReturnsPayloadWithUserLocalContext() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("jack@test.com")
            .username("jack")
            .password("123")
            .build();
    when(userService.createUser(any(RegisterParam.class))).thenReturn(currentUser);

    DataFetcherResult<UserResult> result = mutation.createUser(input);

    assertThat(result.getLocalContext()).isEqualTo(currentUser);
    assertThat(result.getData()).isInstanceOf(UserPayload.class);
  }

  @Test
  void createUserReturnsErrorDataOnConstraintViolation() {
    CreateUserInput input = CreateUserInput.newBuilder().email("bad").build();
    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(new ConstraintViolationException(Collections.emptySet()));

    DataFetcherResult<UserResult> result = mutation.createUser(input);

    assertThat(result.getData()).isInstanceOf(Error.class);
    assertThat(((Error) result.getData()).getMessage()).isEqualTo("BAD_REQUEST");
  }

  @Test
  void loginReturnsPayloadWhenPasswordMatches() {
    when(userRepository.findByEmail("jack@test.com")).thenReturn(Optional.of(currentUser));
    when(encryptService.matches("123", currentUser.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result = mutation.login("123", "jack@test.com");

    assertThat(result.getLocalContext()).isEqualTo(currentUser);
  }

  @Test
  void loginThrowsWhenPasswordDoesNotMatch() {
    when(userRepository.findByEmail("jack@test.com")).thenReturn(Optional.of(currentUser));
    when(encryptService.matches("bad", currentUser.getPassword())).thenReturn(false);

    assertThatThrownBy(() -> mutation.login("bad", "jack@test.com"))
        .isInstanceOf(InvalidAuthenticationException.class);
  }

  @Test
  void loginThrowsWhenUserMissing() {
    when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.login("123", "ghost@test.com"))
        .isInstanceOf(InvalidAuthenticationException.class);
  }

  @Test
  void updateUserDelegatesToUserService() {
    authenticate(currentUser);
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .username("newname")
            .email("new@test.com")
            .bio("new bio")
            .password("newpass")
            .image("new image")
            .build();

    DataFetcherResult<UserPayload> result = mutation.updateUser(input);

    verify(userService).updateUser(any(UpdateUserCommand.class));
    assertThat(result.getLocalContext()).isEqualTo(currentUser);
  }

  @Test
  void updateUserReturnsNullForAnonymous() {
    anonymous();

    assertThat(mutation.updateUser(UpdateUserInput.newBuilder().build())).isNull();
  }
}
