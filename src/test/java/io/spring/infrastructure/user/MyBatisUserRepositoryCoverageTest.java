package io.spring.infrastructure.user;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(MyBatisUserRepository.class)
public class MyBatisUserRepositoryCoverageTest extends DbTestBase {
  @Autowired private UserRepository userRepository;

  @Test
  public void should_return_empty_when_find_by_email_not_found() {
    Optional<User> result = userRepository.findByEmail("nonexistent@test.com");
    Assertions.assertFalse(result.isPresent());
  }
}
