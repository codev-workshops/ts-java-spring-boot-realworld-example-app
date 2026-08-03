package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SecurityUtilTest extends GraphQLTestBase {

  @Test
  void should_return_current_user_when_authenticated() {
    authenticate(CURRENT_USER);
    Optional<User> user = SecurityUtil.getCurrentUser();
    assertThat(user).containsSame(CURRENT_USER);
  }

  @Test
  void should_return_empty_when_anonymous() {
    anonymous();
    assertThat(SecurityUtil.getCurrentUser()).isEmpty();
  }
}
