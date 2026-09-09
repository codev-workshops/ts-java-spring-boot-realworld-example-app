package io.spring.api;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user registration and login.
 *
 * <p>Has no class-level {@code @RequestMapping}; its handlers are mapped to {@code /users} and
 * {@code /users/login}. Both endpoints are publicly accessible and return a freshly issued JWT.
 */
@RestController
@AllArgsConstructor
public class UsersApi {
  private UserRepository userRepository;
  private UserQueryService userQueryService;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private UserService userService;

  /**
   * Handles {@code POST /users} and registers a new user.
   *
   * @param registerParam request body wrapped in a {@code "user"} root element holding email,
   *     username and password; validated with bean validation including duplicate email/username
   *     checks
   * @return {@code 201 Created} with a body of the form {@code {"user": UserWithToken}} containing
   *     the new user's data and a JWT
   * @throws org.springframework.web.bind.MethodArgumentNotValidException if the request body fails
   *     bean validation (rendered as {@code 422 Unprocessable Entity} by the global exception
   *     handler)
   * @throws javax.validation.ConstraintViolationException if validation of the parameter fails
   *     again inside the {@code @Validated} {@link UserService} (also rendered as {@code 422})
   */
  @RequestMapping(path = "/users", method = POST)
  public ResponseEntity createUser(@Valid @RequestBody RegisterParam registerParam) {
    User user = userService.createUser(registerParam);
    UserData userData = userQueryService.findById(user.getId()).get();
    return ResponseEntity.status(201)
        .body(userResponse(new UserWithToken(userData, jwtService.toToken(user))));
  }

  /**
   * Handles {@code POST /users/login} and authenticates a user by email and password.
   *
   * @param loginParam request body wrapped in a {@code "user"} root element holding a well-formed
   *     email and a non-blank password
   * @return {@code 200 OK} with a body of the form {@code {"user": UserWithToken}} containing the
   *     user's data and a JWT
   * @throws InvalidAuthenticationException if no user has the given email or the password does not
   *     match (rendered as {@code 422 Unprocessable Entity} with an error message)
   * @throws org.springframework.web.bind.MethodArgumentNotValidException if the request body fails
   *     bean validation (rendered as {@code 422 Unprocessable Entity})
   */
  @RequestMapping(path = "/users/login", method = POST)
  public ResponseEntity userLogin(@Valid @RequestBody LoginParam loginParam) {
    Optional<User> optional = userRepository.findByEmail(loginParam.getEmail());
    if (optional.isPresent()
        && passwordEncoder.matches(loginParam.getPassword(), optional.get().getPassword())) {
      UserData userData = userQueryService.findById(optional.get().getId()).get();
      return ResponseEntity.ok(
          userResponse(new UserWithToken(userData, jwtService.toToken(optional.get()))));
    } else {
      throw new InvalidAuthenticationException();
    }
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
  }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class LoginParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  private String email;

  @NotBlank(message = "can't be empty")
  private String password;
}
