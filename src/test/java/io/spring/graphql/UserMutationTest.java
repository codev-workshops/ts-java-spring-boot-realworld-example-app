package io.spring.graphql;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserMutationTest extends GraphQLTestBase {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;
  @InjectMocks private UserMutation userMutation;

  @Test
  void should_create_user() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("john@jacob.com")
            .username("johnjacob")
            .password("123")
            .build();
    when(userService.createUser(any(RegisterParam.class))).thenReturn(CURRENT_USER);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertThat(result.getData()).isInstanceOf(UserPayload.class);
    assertThat(result.getLocalContext()).isSameAs(CURRENT_USER);
  }

  @Test
  void should_return_error_data_when_creation_violates_constraints() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("invalid").username("").password("").build();
    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(new ConstraintViolationException(Collections.emptySet()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertThat(result.getData()).isInstanceOf(Error.class);
    assertThat(((Error) result.getData()).getMessage()).isEqualTo("BAD_REQUEST");
  }

  @Test
  void should_login_with_matching_password() {
    when(userRepository.findByEmail("john@jacob.com")).thenReturn(Optional.of(CURRENT_USER));
    when(encryptService.matches("123", CURRENT_USER.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("123", "john@jacob.com");

    assertThat(result.getLocalContext()).isSameAs(CURRENT_USER);
  }

  @Test
  void should_throw_when_password_does_not_match() {
    when(userRepository.findByEmail("john@jacob.com")).thenReturn(Optional.of(CURRENT_USER));
    when(encryptService.matches("wrong", CURRENT_USER.getPassword())).thenReturn(false);

    assertThatThrownBy(() -> userMutation.login("wrong", "john@jacob.com"))
        .isInstanceOf(InvalidAuthenticationException.class);
  }

  @Test
  void should_throw_when_user_email_not_found() {
    when(userRepository.findByEmail("none@jacob.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userMutation.login("123", "none@jacob.com"))
        .isInstanceOf(InvalidAuthenticationException.class);
  }

  @Test
  void should_update_current_user() {
    authenticate(CURRENT_USER);
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@jacob.com")
            .username("newname")
            .bio("new bio")
            .password("newpass")
            .image("new image")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    ArgumentCaptor<UpdateUserCommand> captor = ArgumentCaptor.forClass(UpdateUserCommand.class);
    verify(userService).updateUser(captor.capture());
    User target = captor.getValue().getTargetUser();
    assertThat(target).isSameAs(CURRENT_USER);
    assertThat(captor.getValue().getParam().getEmail()).isEqualTo("new@jacob.com");
    assertThat(result.getLocalContext()).isSameAs(CURRENT_USER);
  }

  @Test
  void should_return_null_on_update_when_anonymous() {
    anonymous();
    assertThat(userMutation.updateUser(UpdateUserInput.newBuilder().build())).isNull();
  }
}
