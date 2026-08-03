package io.spring.graphql;

import static io.spring.graphql.GraphQLTestFixtures.anonymous;
import static io.spring.graphql.GraphQLTestFixtures.authenticate;
import static io.spring.graphql.GraphQLTestFixtures.clearAuthentication;
import static io.spring.graphql.GraphQLTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.spring.application.TagsQueryService;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SecurityUtilAndTagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;

  @AfterEach
  void tearDown() {
    clearAuthentication();
  }

  @Test
  void getCurrentUserReturnsAuthenticatedPrincipal() {
    User currentUser = user("jack");
    authenticate(currentUser);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertThat(result).contains(currentUser);
  }

  @Test
  void getCurrentUserIsEmptyForAnonymous() {
    anonymous();

    assertThat(SecurityUtil.getCurrentUser()).isEmpty();
  }

  /**
   * Documents current behaviour: with no authentication in the context (an unauthenticated call
   * that never went through the JWT filter) {@code getCurrentUser} dereferences a null
   * Authentication and throws. Asserted, not fixed.
   */
  @Test
  void getCurrentUserThrowsWhenNoAuthenticationInContext() {
    clearAuthentication();

    assertThatThrownBy(SecurityUtil::getCurrentUser).isInstanceOf(NullPointerException.class);
  }

  @Test
  void getTagsDelegatesToQueryService() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring"));

    assertThat(new TagDatafetcher(tagsQueryService).getTags()).containsExactly("java", "spring");
  }
}
