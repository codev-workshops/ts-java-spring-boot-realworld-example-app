package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class SecurityUtilTest extends GraphQLTestBase {

  @Test
  public void should_return_current_user_when_authenticated() {
    User user = new User("a@test.com", "alice", "123", "", "");
    setAuthenticatedUser(user);
    Optional<User> current = SecurityUtil.getCurrentUser();
    assertTrue(current.isPresent());
    assertTrue(current.get() == user);
  }

  @Test
  public void should_return_empty_when_anonymous() {
    setAnonymous();
    assertFalse(SecurityUtil.getCurrentUser().isPresent());
  }
}
