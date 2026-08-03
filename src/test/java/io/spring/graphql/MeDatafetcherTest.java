package io.spring.graphql;

import static io.spring.graphql.GraphQLTestFixtures.anonymous;
import static io.spring.graphql.GraphQLTestFixtures.authenticate;
import static io.spring.graphql.GraphQLTestFixtures.clearAuthentication;
import static io.spring.graphql.GraphQLTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.graphql.types.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;
  @Mock private DataFetchingEnvironment dfe;

  private MeDatafetcher datafetcher;
  private io.spring.core.user.User currentUser;

  @BeforeEach
  void setUp() {
    datafetcher = new MeDatafetcher(userQueryService, jwtService);
    currentUser = user("jack");
  }

  @AfterEach
  void tearDown() {
    clearAuthentication();
  }

  @Test
  void getMeReturnsCurrentUserWithToken() {
    authenticate(currentUser);
    UserData userData = new UserData(currentUser.getId(), "jack@test.com", "jack", "bio", "image");
    when(userQueryService.findById(currentUser.getId())).thenReturn(Optional.of(userData));

    DataFetcherResult<User> result = datafetcher.getMe("Token jwt-token", dfe);

    assertThat(result.getData().getUsername()).isEqualTo("jack");
    assertThat(result.getData().getToken()).isEqualTo("jwt-token");
    assertThat(result.getLocalContext()).isEqualTo(currentUser);
  }

  @Test
  void getMeReturnsNullForAnonymous() {
    anonymous();

    assertThat(datafetcher.getMe("Token jwt-token", dfe)).isNull();
  }

  @Test
  void getMeThrowsWhenUserDataMissing() {
    authenticate(currentUser);
    when(userQueryService.findById(currentUser.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> datafetcher.getMe("Token jwt-token", dfe))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getUserPayloadUserBuildsTokenFromJwtService() {
    when(dfe.<io.spring.core.user.User>getLocalContext()).thenReturn(currentUser);
    when(jwtService.toToken(currentUser)).thenReturn("signed-token");

    DataFetcherResult<User> result = datafetcher.getUserPayloadUser(dfe);

    assertThat(result.getData().getToken()).isEqualTo("signed-token");
    assertThat(result.getData().getEmail()).isEqualTo("jack@test.com");
  }
}
