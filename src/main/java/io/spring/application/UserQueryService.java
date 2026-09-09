package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/** Read-side application service for {@link UserData} read models. */
@Service
@AllArgsConstructor
public class UserQueryService {
  private UserReadService userReadService;

  /**
   * Finds a user's account data by id.
   *
   * @param id the user id
   * @return the user data, or {@link Optional#empty()} if no user has the given id
   */
  public Optional<UserData> findById(String id) {
    return Optional.ofNullable(userReadService.findById(id));
  }
}
