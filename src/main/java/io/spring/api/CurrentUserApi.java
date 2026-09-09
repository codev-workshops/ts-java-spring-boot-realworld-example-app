package io.spring.api;

import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the currently authenticated user's own account.
 *
 * <p>Handles requests under the base path {@code /user}: reading and updating the current user.
 * Both endpoints echo back the bearer token taken from the {@code Authorization} header so the
 * client can keep using it.
 */
@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class CurrentUserApi {

  private UserQueryService userQueryService;
  private UserService userService;

  /**
   * Handles {@code GET /user} and returns the current user's account data.
   *
   * @param currentUser the currently authenticated user
   * @param authorization the raw {@code Authorization} header, expected in the form {@code "Token
   *     <jwt>"}; the token part is echoed back in the response
   * @return {@code 200 OK} with a body of the form {@code {"user": UserWithToken}} containing
   *     email, username, bio, image and token
   * @throws ArrayIndexOutOfBoundsException if the {@code Authorization} header does not contain a
   *     space-separated scheme and token
   */
  @GetMapping
  public ResponseEntity currentUser(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader(value = "Authorization") String authorization) {
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return ResponseEntity.ok(
        userResponse(new UserWithToken(userData, authorization.split(" ")[1])));
  }

  /**
   * Handles {@code PUT /user} and updates the current user's email, username, password, bio and/or
   * image.
   *
   * @param currentUser the currently authenticated user whose account is updated
   * @param token the raw {@code Authorization} header, expected in the form {@code "Token <jwt>"};
   *     the token part is echoed back in the response
   * @param updateUserParam request body wrapped in a {@code "user"} root element holding the fields
   *     to change; blank fields are left unchanged
   * @return {@code 200 OK} with a body of the form {@code {"user": UserWithToken}} describing the
   *     updated account
   * @throws org.springframework.web.bind.MethodArgumentNotValidException if the request body fails
   *     bean validation, e.g. a malformed email (rendered as {@code 422 Unprocessable Entity})
   * @throws javax.validation.ConstraintViolationException if the new email or username is already
   *     taken by another user, as detected by the {@code @UpdateUserConstraint} on the command
   *     inside {@link UserService#updateUser} (rendered as {@code 422 Unprocessable Entity})
   * @throws ArrayIndexOutOfBoundsException if the {@code Authorization} header does not contain a
   *     space-separated scheme and token
   */
  @PutMapping
  public ResponseEntity updateProfile(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader("Authorization") String token,
      @Valid @RequestBody UpdateUserParam updateUserParam) {

    userService.updateUser(new UpdateUserCommand(currentUser, updateUserParam));
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return ResponseEntity.ok(userResponse(new UserWithToken(userData, token.split(" ")[1])));
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
  }
}
