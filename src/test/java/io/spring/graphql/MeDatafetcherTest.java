package io.spring.graphql;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeDatafetcherTest extends GraphQLTestBase {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;
  @InjectMocks private MeDatafetcher meDatafetcher;

  private final UserData userData =
      new UserData(CURRENT_USER.getId(), "john@jacob.com", "johnjacob", "bio", "image");

  @Test
  void should_build_me_result_from_authorization_header() {
    authenticate(CURRENT_USER);
    when(userQueryService.findById(CURRENT_USER.getId())).thenReturn(Optional.of(userData));

    DataFetcherResult<User> result =
        meDatafetcher.getMe("Token jwt-token", dataFetchingEnvironment);

    assertThat(result.getData().getEmail()).isEqualTo("john@jacob.com");
    assertThat(result.getData().getUsername()).isEqualTo("johnjacob");
    assertThat(result.getData().getToken()).isEqualTo("jwt-token");
    assertThat(result.getLocalContext()).isSameAs(CURRENT_USER);
  }

  @Test
  void should_return_null_when_anonymous() {
    anonymous();
    assertThat(meDatafetcher.getMe("Token jwt-token", dataFetchingEnvironment)).isNull();
  }

  @Test
  void should_throw_when_user_not_found() {
    authenticate(CURRENT_USER);
    when(userQueryService.findById(CURRENT_USER.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> meDatafetcher.getMe("Token jwt-token", dataFetchingEnvironment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_build_user_payload_user_with_generated_token() {
    when(dataFetchingEnvironment.<io.spring.core.user.User>getLocalContext())
        .thenReturn(CURRENT_USER);
    when(jwtService.toToken(CURRENT_USER)).thenReturn("new-token");

    DataFetcherResult<User> result = meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertThat(result.getData().getToken()).isEqualTo("new-token");
    assertThat(result.getData().getUsername()).isEqualTo("johnjacob");
    assertThat(result.getLocalContext()).isSameAs(CURRENT_USER);
  }
}
