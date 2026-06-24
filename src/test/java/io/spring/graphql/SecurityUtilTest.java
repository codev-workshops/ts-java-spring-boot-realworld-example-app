package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilTest {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void testGetCurrentUserAuthenticated() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "img");
    Authentication auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
  }

  @Test
  void testGetCurrentUserAnonymous() {
    AnonymousAuthenticationToken anonAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", Collections.singletonList(() -> "ROLE_ANONYMOUS"));
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(anonAuth);
    SecurityContextHolder.setContext(context);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertFalse(result.isPresent());
  }

  @Test
  void testGetCurrentUserNullPrincipal() {
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(null);
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertFalse(result.isPresent());
  }
}
