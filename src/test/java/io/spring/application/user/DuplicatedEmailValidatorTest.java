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

public class DuplicatedEmailValidatorTest {

  @Mock private UserRepository userRepository;
  private DuplicatedEmailValidator validator;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new DuplicatedEmailValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
  }

  @Test
  public void should_be_valid_when_email_empty() {
    assertTrue(validator.isValid("", null));
    assertTrue(validator.isValid(null, null));
  }

  @Test
  public void should_be_valid_when_email_not_used() {
    when(userRepository.findByEmail("free@test.com")).thenReturn(Optional.empty());
    assertTrue(validator.isValid("free@test.com", null));
  }

  @Test
  public void should_be_invalid_when_email_used() {
    when(userRepository.findByEmail("used@test.com"))
        .thenReturn(Optional.of(new User("used@test.com", "u", "p", "", "")));
    assertFalse(validator.isValid("used@test.com", null));
  }
}
