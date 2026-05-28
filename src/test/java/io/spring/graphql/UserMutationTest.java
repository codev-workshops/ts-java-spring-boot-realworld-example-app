package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    userMutation = new UserMutation(userRepository, encryptService, userService);
    user = new User("test@test.com", "testuser", "pass", "", "");
  }

  @AfterEach
  void tearDown() throws Exception {
    SecurityContextHolder.clearContext();
    closeable.close();
  }

  @Test
  void should_create_user() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .password("password")
            .build();
    User newUser = new User("new@test.com", "newuser", "pass", "", "");
    when(userService.createUser(any())).thenReturn(newUser);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
    assertNotNull(result.getLocalContext());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_return_errors_on_constraint_violation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("dup@test.com")
            .username("dupuser")
            .password("password")
            .build();

    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) User.class);
    when(violation.getMessage()).thenReturn("email already exist");
    when(violation.getPropertyPath()).thenReturn(new TestPath("email"));

    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    HashSet<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);
    when(userService.createUser(any())).thenThrow(cve);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    assertNotNull(result);
  }

  @Test
  void should_login_successfully() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("pass", user.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("pass", "test@test.com");
    assertNotNull(result);
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_when_login_with_wrong_password() {
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", user.getPassword())).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("wrong", "test@test.com"));
  }

  @Test
  void should_throw_when_login_with_unknown_email() {
    when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class, () -> userMutation.login("pass", "unknown@test.com"));
  }

  @Test
  void should_update_user() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input =
        UpdateUserInput.newBuilder().email("updated@test.com").username("updateduser").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);
    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_anonymous_for_update() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@test.com").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);
    assertNull(result);
  }

  @Test
  void should_return_null_when_principal_null_for_update() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(null, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@test.com").build();
    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);
    assertNull(result);
  }

  static class TestPath implements Path {
    private final String path;

    TestPath(String path) {
      this.path = path;
    }

    @Override
    public java.util.Iterator<Node> iterator() {
      return Collections.emptyIterator();
    }

    @Override
    public String toString() {
      return path;
    }
  }
}
