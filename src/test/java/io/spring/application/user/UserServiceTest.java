package io.spring.application.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

public class UserServiceTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserService userService;

  @BeforeEach
  public void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = new UserService(userRepository, "default-image", passwordEncoder);
  }

  @Test
  public void should_create_user_with_encoded_password_and_default_image() {
    when(passwordEncoder.encode("123")).thenReturn("encoded");
    RegisterParam param = new RegisterParam("a@test.com", "aisensiy", "123");

    User user = userService.createUser(param);

    Assertions.assertEquals("a@test.com", user.getEmail());
    Assertions.assertEquals("aisensiy", user.getUsername());
    Assertions.assertEquals("encoded", user.getPassword());
    Assertions.assertEquals("default-image", user.getImage());
    verify(userRepository).save(user);
  }

  @Test
  public void should_update_user_and_save() {
    User target = new User("a@test.com", "aisensiy", "123", "old-bio", "old-image");
    UpdateUserParam param =
        UpdateUserParam.builder().email("new@test.com").username("newname").bio("new-bio").build();

    userService.updateUser(new UpdateUserCommand(target, param));

    Assertions.assertEquals("new@test.com", target.getEmail());
    Assertions.assertEquals("newname", target.getUsername());
    Assertions.assertEquals("new-bio", target.getBio());
    verify(userRepository).save(target);
  }

  @Test
  public void duplicated_email_validator_allows_null_or_empty() {
    DuplicatedEmailValidator validator = new DuplicatedEmailValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);

    Assertions.assertTrue(validator.isValid(null, null));
    Assertions.assertTrue(validator.isValid("", null));
    verify(userRepository, never()).findByEmail(any());
  }

  @Test
  public void duplicated_email_validator_rejects_existing_email() {
    DuplicatedEmailValidator validator = new DuplicatedEmailValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    when(userRepository.findByEmail("taken@test.com"))
        .thenReturn(Optional.of(new User("taken@test.com", "u", "p", "", "")));

    Assertions.assertFalse(validator.isValid("taken@test.com", null));
  }

  @Test
  public void duplicated_email_validator_allows_new_email() {
    DuplicatedEmailValidator validator = new DuplicatedEmailValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    when(userRepository.findByEmail("free@test.com")).thenReturn(Optional.empty());

    Assertions.assertTrue(validator.isValid("free@test.com", null));
  }

  @Test
  public void duplicated_username_validator_allows_null_or_empty() {
    DuplicatedUsernameValidator validator = new DuplicatedUsernameValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);

    Assertions.assertTrue(validator.isValid(null, null));
    Assertions.assertTrue(validator.isValid("", null));
    verify(userRepository, never()).findByUsername(any());
  }

  @Test
  public void duplicated_username_validator_rejects_existing_username() {
    DuplicatedUsernameValidator validator = new DuplicatedUsernameValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    when(userRepository.findByUsername("taken"))
        .thenReturn(Optional.of(new User("u@test.com", "taken", "p", "", "")));

    Assertions.assertFalse(validator.isValid("taken", null));
  }

  @Test
  public void duplicated_username_validator_allows_new_username() {
    DuplicatedUsernameValidator validator = new DuplicatedUsernameValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    when(userRepository.findByUsername("free")).thenReturn(Optional.empty());

    Assertions.assertTrue(validator.isValid("free", null));
  }

  @Test
  public void update_user_validator_allows_when_no_conflict() {
    UpdateUserValidator validator = new UpdateUserValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    User target = new User("a@test.com", "aisensiy", "123", "", "");
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

    UpdateUserParam param =
        UpdateUserParam.builder().email("new@test.com").username("newname").build();
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    Assertions.assertTrue(validator.isValid(new UpdateUserCommand(target, param), context));
  }

  @Test
  public void update_user_validator_allows_when_conflict_is_same_user() {
    UpdateUserValidator validator = new UpdateUserValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    User target = new User("a@test.com", "aisensiy", "123", "", "");
    when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(target));
    when(userRepository.findByUsername("aisensiy")).thenReturn(Optional.of(target));

    UpdateUserParam param =
        UpdateUserParam.builder().email("a@test.com").username("aisensiy").build();
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    Assertions.assertTrue(validator.isValid(new UpdateUserCommand(target, param), context));
  }

  @Test
  public void update_user_validator_rejects_when_email_and_username_taken() {
    UpdateUserValidator validator = new UpdateUserValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    User target = new User("a@test.com", "aisensiy", "123", "", "");
    User other = new User("other@test.com", "other", "123", "", "");
    when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(other));
    when(userRepository.findByUsername("other")).thenReturn(Optional.of(other));

    UpdateUserParam param =
        UpdateUserParam.builder().email("other@test.com").username("other").build();
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);

    Assertions.assertFalse(validator.isValid(new UpdateUserCommand(target, param), context));
    verify(context).disableDefaultConstraintViolation();
    ArgumentCaptor<String> messages = ArgumentCaptor.forClass(String.class);
    verify(context, times(2)).buildConstraintViolationWithTemplate(messages.capture());
    Assertions.assertTrue(messages.getAllValues().contains("email already exist"));
    Assertions.assertTrue(messages.getAllValues().contains("username already exist"));
  }

  @Test
  public void update_user_validator_rejects_when_only_username_taken() {
    UpdateUserValidator validator = new UpdateUserValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    User target = new User("a@test.com", "aisensiy", "123", "", "");
    User other = new User("other@test.com", "other", "123", "", "");
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(userRepository.findByUsername("other")).thenReturn(Optional.of(other));

    UpdateUserParam param =
        UpdateUserParam.builder().email("free@test.com").username("other").build();
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);

    Assertions.assertFalse(validator.isValid(new UpdateUserCommand(target, param), context));
    verify(context).buildConstraintViolationWithTemplate(eq("username already exist"));
    verify(context, never()).buildConstraintViolationWithTemplate(eq("email already exist"));
  }

  @Test
  public void update_user_validator_rejects_when_only_email_taken() {
    UpdateUserValidator validator = new UpdateUserValidator();
    ReflectionTestUtils.setField(validator, "userRepository", userRepository);
    User target = new User("a@test.com", "aisensiy", "123", "", "");
    User other = new User("other@test.com", "other", "123", "", "");
    when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(other));
    when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

    UpdateUserParam param =
        UpdateUserParam.builder().email("other@test.com").username("freename").build();
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);

    Assertions.assertFalse(validator.isValid(new UpdateUserCommand(target, param), context));
    verify(context).buildConstraintViolationWithTemplate(eq("email already exist"));
    verify(context, never()).buildConstraintViolationWithTemplate(eq("username already exist"));
  }
}
