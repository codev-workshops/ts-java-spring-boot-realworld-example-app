package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DuplicatedUsernameValidatorTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private DuplicatedUsernameValidator validator;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void should_return_true_when_username_is_null() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  void should_return_true_when_username_is_empty() {
    assertTrue(validator.isValid("", null));
  }

  @Test
  void should_return_true_when_username_not_duplicated() {
    when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
    assertTrue(validator.isValid("newuser", null));
  }

  @Test
  void should_return_false_when_username_is_duplicated() {
    User existing = new User("test@test.com", "existing", "pass", "", "");
    when(userRepository.findByUsername("existing")).thenReturn(Optional.of(existing));
    assertFalse(validator.isValid("existing", null));
  }
}
