package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;

  @BeforeEach
  void setUp() {
    userMutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("test@email.com", "testuser", "pass", "", "");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(u, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void createUser_success() {
    User newUser = new User("new@email.com", "newuser", "encoded", "", "default.jpg");
    when(userService.createUser(any())).thenReturn(newUser);

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@email.com")
            .username("newuser")
            .password("password")
            .build();

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertInstanceOf(UserPayload.class, result.getData());
  }

  @Test
  void createUser_returns_error_on_constraint_violation() {
    ConstraintViolationException cve = buildConstraintViolationException();
    when(userService.createUser(any())).thenThrow(cve);

    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("dup@email.com")
            .username("dupuser")
            .password("pass")
            .build();

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertInstanceOf(io.spring.graphql.types.Error.class, result.getData());
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolationException buildConstraintViolationException() {
    ConstraintViolation<Object> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
    org.mockito.Mockito.doReturn((Class) Object.class).when(violation).getRootBeanClass();

    Path path = org.mockito.Mockito.mock(Path.class);
    org.mockito.Mockito.doReturn("registerParam.email").when(path).toString();
    org.mockito.Mockito.doReturn(path).when(violation).getPropertyPath();
    org.mockito.Mockito.doReturn("email already exist").when(violation).getMessage();

    ConstraintDescriptor<?> descriptor = org.mockito.Mockito.mock(ConstraintDescriptor.class);
    org.mockito.Mockito.doReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }

              @Override
              public String message() {
                return "";
              }

              @Override
              public Class<?>[] groups() {
                return new Class[0];
              }

              @Override
              public Class<? extends javax.validation.Payload>[] payload() {
                return new Class[0];
              }
            })
        .when(descriptor)
        .getAnnotation();
    org.mockito.Mockito.doReturn(descriptor).when(violation).getConstraintDescriptor();

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    return new ConstraintViolationException(violations);
  }

  @Test
  void login_success() {
    when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("pass", "pass")).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("pass", "test@email.com");

    assertNotNull(result);
  }

  @Test
  void login_throws_on_invalid_credentials() {
    when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", "pass")).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@email.com"));
  }

  @Test
  void login_throws_when_user_not_found() {
    when(userRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("pass", "missing@email.com"));
  }

  @Test
  void updateUser_success_when_authenticated() {
    setAuthenticated(user);

    UpdateUserInput input = UpdateUserInput.newBuilder().email("updated@email.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNotNull(result);
  }

  @Test
  void updateUser_returns_null_for_anonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input = UpdateUserInput.newBuilder().email("x@y.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
  }
}
