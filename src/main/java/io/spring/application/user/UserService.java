package io.spring.application.user;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Write-side application service for user accounts.
 *
 * <p>Annotated with {@code @Validated}, so parameters marked {@code @Valid} are checked with bean
 * validation before the method body runs.
 */
@Service
@Validated
public class UserService {
  private UserRepository userRepository;
  private String defaultImage;
  private PasswordEncoder passwordEncoder;

  /**
   * Creates the service.
   *
   * @param userRepository repository used to persist users
   * @param defaultImage URL of the profile image assigned to newly registered users, injected from
   *     the {@code image.default} property
   * @param passwordEncoder encoder used to hash passwords before storing them
   */
  @Autowired
  public UserService(
      UserRepository userRepository,
      @Value("${image.default}") String defaultImage,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.defaultImage = defaultImage;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Registers a new user with an empty bio and the default profile image. The password is hashed
   * before being stored.
   *
   * @param registerParam email, username and plain-text password of the new user; validated with
   *     bean validation including duplicate email/username checks
   * @return the newly created and saved user
   * @throws javax.validation.ConstraintViolationException if {@code registerParam} fails validation
   */
  public User createUser(@Valid RegisterParam registerParam) {
    User user =
        new User(
            registerParam.getEmail(),
            registerParam.getUsername(),
            passwordEncoder.encode(registerParam.getPassword()),
            "",
            defaultImage);
    userRepository.save(user);
    return user;
  }

  /**
   * Updates the email, username, password, bio and image of an existing user and persists it. Blank
   * values leave the corresponding field unchanged.
   *
   * @param command the target user together with the new field values; validated by {@link
   *     UpdateUserValidator}, which rejects an email or username already used by another user
   * @throws javax.validation.ConstraintViolationException if {@code command} fails validation
   */
  public void updateUser(@Valid UpdateUserCommand command) {
    User user = command.getTargetUser();
    UpdateUserParam updateUserParam = command.getParam();
    user.update(
        updateUserParam.getEmail(),
        updateUserParam.getUsername(),
        updateUserParam.getPassword(),
        updateUserParam.getBio(),
        updateUserParam.getImage());
    userRepository.save(user);
  }
}

@Constraint(validatedBy = UpdateUserValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@interface UpdateUserConstraint {

  String message() default "invalid update param";

  Class[] groups() default {};

  Class[] payload() default {};
}

/**
 * Validator backing {@link UpdateUserConstraint}: ensures the email and username requested in an
 * update are not already taken by a different user.
 */
class UpdateUserValidator implements ConstraintValidator<UpdateUserConstraint, UpdateUserCommand> {

  @Autowired private UserRepository userRepository;

  /**
   * Checks the requested email and username for collisions with other users.
   *
   * <p>Each value is looked up in the repository. If no user has it, the value is valid ({@code
   * orElse(true)}). If a user has it, the value is valid only when that user equals the target
   * user, so a user may keep (or resubmit) their own email/username. When either check fails the
   * default violation is disabled and a dedicated violation is added per failing field: {@code
   * "email already exist"} on the {@code email} property node and/or {@code "username already
   * exist"} on the {@code username} property node.
   *
   * @param value the update command holding the target user and the requested new values
   * @param context the validator context used to register per-field violations
   * @return {@code true} if neither the email nor the username collides with another user, {@code
   *     false} otherwise
   */
  @Override
  public boolean isValid(UpdateUserCommand value, ConstraintValidatorContext context) {
    String inputEmail = value.getParam().getEmail();
    String inputUsername = value.getParam().getUsername();
    final User targetUser = value.getTargetUser();

    boolean isEmailValid =
        userRepository.findByEmail(inputEmail).map(user -> user.equals(targetUser)).orElse(true);
    boolean isUsernameValid =
        userRepository
            .findByUsername(inputUsername)
            .map(user -> user.equals(targetUser))
            .orElse(true);
    if (isEmailValid && isUsernameValid) {
      return true;
    } else {
      context.disableDefaultConstraintViolation();
      if (!isEmailValid) {
        context
            .buildConstraintViolationWithTemplate("email already exist")
            .addPropertyNode("email")
            .addConstraintViolation();
      }
      if (!isUsernameValid) {
        context
            .buildConstraintViolationWithTemplate("username already exist")
            .addPropertyNode("username")
            .addConstraintViolation();
      }
      return false;
    }
  }
}
