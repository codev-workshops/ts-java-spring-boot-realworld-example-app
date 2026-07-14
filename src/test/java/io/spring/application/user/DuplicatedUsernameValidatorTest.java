package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class DuplicatedUsernameValidatorTest {

  @Mock private UserRepository userRepository;
  private DuplicatedUsernameValidator validator;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new DuplicatedUsernameValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
  }

  @Test
  public void should_be_valid_when_username_empty() {
    assertTrue(validator.isValid("", null));
    assertTrue(validator.isValid(null, null));
  }

  @Test
  public void should_be_valid_when_username_not_used() {
    when(userRepository.findByUsername("free")).thenReturn(Optional.empty());
    assertTrue(validator.isValid("free", null));
  }

  @Test
  public void should_be_invalid_when_username_used() {
    when(userRepository.findByUsername("used"))
        .thenReturn(Optional.of(new User("u@test.com", "used", "p", "", "")));
    assertFalse(validator.isValid("used", null));
  }
}
