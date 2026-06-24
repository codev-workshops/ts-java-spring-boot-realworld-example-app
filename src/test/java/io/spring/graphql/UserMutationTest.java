package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import java.util.Collections;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation mutation;

  @BeforeEach
  void setUp() {
    mutation = new UserMutation(userRepository, encryptService, userService);
  }

  @Test
  void testCreateUserSuccess() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("pass")
            .build();

    User user = new User("test@test.com", "testuser", "pass", "bio", "img");
    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = mutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof UserPayload);
  }

  @Test
  void testCreateUserConstraintViolation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("pass")
            .build();

    ConstraintViolationException cve =
        new ConstraintViolationException("error", Collections.emptySet());
    when(userService.createUser(any())).thenThrow(cve);

    DataFetcherResult<UserResult> result = mutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void testLoginSuccess() {
    User user = new User("test@test.com", "testuser", "encodedPass", "bio", "img");
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("pass", user.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result = mutation.login("pass", "test@test.com");

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void testLoginInvalidPassword() {
    User user = new User("test@test.com", "testuser", "encodedPass", "bio", "img");
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrong", user.getPassword())).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class, () -> mutation.login("wrong", "test@test.com"));
  }

  @Test
  void testLoginUserNotFound() {
    when(userRepository.findByEmail("nonexist@test.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class, () -> mutation.login("pass", "nonexist@test.com"));
  }

  @Test
  void testUpdateUserSuccess() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "img");
    Authentication auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@test.com")
            .username("newuser")
            .bio("new bio")
            .image("new img")
            .password("newpass")
            .build();

    DataFetcherResult<UserPayload> result = mutation.updateUser(input);

    assertNotNull(result);
    verify(userService).updateUser(any());

    SecurityContextHolder.clearContext();
  }

  @Test
  void testUpdateUserAnonymous() {
    AnonymousAuthenticationToken anonAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", Collections.singletonList(() -> "ROLE_ANONYMOUS"));
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(anonAuth);
    SecurityContextHolder.setContext(context);

    UpdateUserInput input = UpdateUserInput.newBuilder().email("e").build();

    DataFetcherResult<UserPayload> result = mutation.updateUser(input);

    assertNull(result);

    SecurityContextHolder.clearContext();
  }

  @Test
  void testUpdateUserNullPrincipal() {
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(null);
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    UpdateUserInput input = UpdateUserInput.newBuilder().email("e").build();

    DataFetcherResult<UserPayload> result = mutation.updateUser(input);

    assertNull(result);

    SecurityContextHolder.clearContext();
  }
}
