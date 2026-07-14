package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.graphql.types.UserPayload;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MeDatafetcherTest extends GraphQLTestBase {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;
  @Mock private DataFetchingEnvironment dfe;

  private MeDatafetcher meDatafetcher;
  private User user;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    meDatafetcher = new MeDatafetcher(userQueryService, jwtService);
    user = new User("a@test.com", "alice", "123", "", "");
  }

  @Test
  public void should_return_me_when_authenticated() {
    setAuthenticatedUser(user);
    UserData userData = new UserData(user.getId(), "a@test.com", "alice", "", "");
    when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Token jwt-token", dfe);

    assertEquals("alice", result.getData().getUsername());
    assertEquals("a@test.com", result.getData().getEmail());
    assertEquals("jwt-token", result.getData().getToken());
  }

  @Test
  public void should_return_null_when_anonymous() {
    setAnonymous();
    assertNull(meDatafetcher.getMe("Token jwt-token", dfe));
  }

  @Test
  public void should_throw_when_user_data_missing() {
    setAuthenticatedUser(user);
    when(userQueryService.findById(user.getId())).thenReturn(Optional.empty());
    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> meDatafetcher.getMe("Token jwt-token", dfe));
  }

  @Test
  public void should_build_user_payload_user_from_local_context() {
    when(dfe.getLocalContext()).thenReturn(user);
    when(jwtService.toToken(user)).thenReturn("generated-token");

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getUserPayloadUser(dfe);

    assertEquals("alice", result.getData().getUsername());
    assertEquals("generated-token", result.getData().getToken());
    // ensure payload type is usable
    UserPayload.newBuilder().build();
  }
}
