package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class UpdateUserValidatorTest {

  @Mock private UserRepository userRepository;
  @Mock private ConstraintValidatorContext context;
  @Mock private ConstraintViolationBuilder violationBuilder;
  @Mock private NodeBuilderCustomizableContext nodeBuilder;

  private UpdateUserValidator validator;
  private User targetUser;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new UpdateUserValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    targetUser = new User("target@test.com", "target", "123", "", "");
    lenient()
        .when(context.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(violationBuilder);
    lenient().when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    lenient().when(nodeBuilder.addConstraintViolation()).thenReturn(context);
  }

  private UpdateUserCommand command(String email, String username) {
    UpdateUserParam param = UpdateUserParam.builder().email(email).username(username).build();
    return new UpdateUserCommand(targetUser, param);
  }

  @Test
  public void should_be_valid_when_email_and_username_free() {
    when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("newname")).thenReturn(Optional.empty());
    assertTrue(validator.isValid(command("new@test.com", "newname"), context));
  }

  @Test
  public void should_be_valid_when_email_and_username_belong_to_target() {
    when(userRepository.findByEmail("target@test.com")).thenReturn(Optional.of(targetUser));
    when(userRepository.findByUsername("target")).thenReturn(Optional.of(targetUser));
    assertTrue(validator.isValid(command("target@test.com", "target"), context));
  }

  @Test
  public void should_be_invalid_when_email_taken_by_other() {
    User other = new User("taken@test.com", "other", "123", "", "");
    when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(other));
    when(userRepository.findByUsername("newname")).thenReturn(Optional.empty());
    assertFalse(validator.isValid(command("taken@test.com", "newname"), context));
    verify(context).buildConstraintViolationWithTemplate("email already exist");
  }

  @Test
  public void should_be_invalid_when_username_taken_by_other() {
    User other = new User("other@test.com", "taken", "123", "", "");
    when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("taken")).thenReturn(Optional.of(other));
    assertFalse(validator.isValid(command("new@test.com", "taken"), context));
    verify(context).buildConstraintViolationWithTemplate("username already exist");
  }
}
