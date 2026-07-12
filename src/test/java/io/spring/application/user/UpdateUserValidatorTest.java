package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdateUserValidatorTest {

  @Mock private UserRepository userRepository;
  @Mock private ConstraintValidatorContext context;
  @Mock private ConstraintViolationBuilder violationBuilder;
  @Mock private NodeBuilderCustomizableContext nodeBuilder;

  @InjectMocks private UpdateUserValidator validator;

  private User targetUser;
  private UpdateUserCommand command;

  @BeforeEach
  public void setUp() {
    targetUser = new User("target@test.com", "target", "123", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder().email("target@test.com").username("target").build();
    command = new UpdateUserCommand(targetUser, param);

    lenient()
        .when(context.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(violationBuilder);
    lenient().when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    lenient().when(nodeBuilder.addConstraintViolation()).thenReturn(context);
  }

  @Test
  public void should_be_valid_when_no_conflicts() {
    when(userRepository.findByEmail("target@test.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("target")).thenReturn(Optional.empty());

    assertTrue(validator.isValid(command, context));
  }

  @Test
  public void should_be_invalid_when_email_taken_by_other_user() {
    User other = new User("target@test.com", "other", "123", "", "");
    when(userRepository.findByEmail("target@test.com")).thenReturn(Optional.of(other));
    when(userRepository.findByUsername("target")).thenReturn(Optional.empty());

    assertFalse(validator.isValid(command, context));
  }

  @Test
  public void should_be_invalid_when_username_taken_by_other_user() {
    User other = new User("other@test.com", "target", "123", "", "");
    when(userRepository.findByEmail("target@test.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(other));

    assertFalse(validator.isValid(command, context));
  }

  @Test
  public void should_be_valid_when_conflicting_user_is_target_user() {
    when(userRepository.findByEmail("target@test.com")).thenReturn(Optional.of(targetUser));
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(targetUser));

    assertTrue(validator.isValid(command, context));
  }
}
