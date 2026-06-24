package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Collections;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private MeDatafetcher fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new MeDatafetcher(userQueryService, jwtService);
  }

  @Test
  void testGetMeAuthenticated() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "img");
    Authentication auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    UserData userData = new UserData("id", "test@test.com", "testuser", "bio", "img");
    when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));

    DataFetcherResult<io.spring.graphql.types.User> result =
        fetcher.getMe("Token mytoken", dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("mytoken", result.getData().getToken());

    SecurityContextHolder.clearContext();
  }

  @Test
  void testGetMeAnonymous() {
    AnonymousAuthenticationToken anonAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", Collections.singletonList(() -> "ROLE_ANONYMOUS"));
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(anonAuth);
    SecurityContextHolder.setContext(context);

    DataFetcherResult<io.spring.graphql.types.User> result =
        fetcher.getMe("Token x", dataFetchingEnvironment);

    assertNull(result);

    SecurityContextHolder.clearContext();
  }

  @Test
  void testGetMeNullPrincipal() {
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(null);
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    DataFetcherResult<io.spring.graphql.types.User> result =
        fetcher.getMe("Token x", dataFetchingEnvironment);

    assertNull(result);

    SecurityContextHolder.clearContext();
  }

  @Test
  void testGetUserPayloadUser() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "img");
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(jwtService.toToken(user)).thenReturn("jwt-token");

    DataFetcherResult<io.spring.graphql.types.User> result =
        fetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test@test.com", result.getData().getEmail());
    assertEquals("testuser", result.getData().getUsername());
    assertEquals("jwt-token", result.getData().getToken());
  }
}
