package io.spring.graphql;

import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/** Shared fixtures and security-context helpers for the GraphQL unit tests. */
final class GraphQLTestFixtures {

  private GraphQLTestFixtures() {}

  static User user(String username) {
    return new User(username + "@test.com", username, "123", "bio", "image");
  }

  static void authenticate(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES));
  }

  static void anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  static void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  static ProfileData profileData(String username) {
    return new ProfileData("profile-id-" + username, username, "bio", "image", false);
  }

  static ArticleData articleData(String id, String slug) {
    return new ArticleData(
        id,
        slug,
        "title",
        "description",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Arrays.asList("java"),
        profileData("author"));
  }

  static CommentData commentData(String id, String articleId) {
    return new CommentData(
        id, "comment body", articleId, new DateTime(), new DateTime(), profileData("author"));
  }

  static java.util.List<String> emptyTags() {
    return Collections.emptyList();
  }
}
