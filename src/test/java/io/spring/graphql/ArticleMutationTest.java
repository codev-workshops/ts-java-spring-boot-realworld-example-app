package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;
  private User user;

  @BeforeEach
  void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@email.com", "testuser", "pass", "", "");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(u, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymous",
            Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void createArticle_success() {
    setAuthenticated(user);
    Article article =
        new Article("Test Title", "desc", "body", Collections.emptyList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void createArticle_throws_when_not_authenticated() {
    setAnonymous();
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("t").description("d").body("b").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void updateArticle_success() {
    setAuthenticated(user);
    Article article =
        new Article("Old Title", "desc", "body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("old-title")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(any(), any())).thenReturn(article);

    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder().title("New Title").description("").body("").build();

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("old-title", changes);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(any(), any());
  }

  @Test
  void updateArticle_throws_when_not_authorized() {
    setAuthenticated(user);
    User other = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), other.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    UpdateArticleInput changes = UpdateArticleInput.newBuilder().build();

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.updateArticle("title", changes));
  }

  @Test
  void favoriteArticle_success() {
    setAuthenticated(user);
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void unfavoriteArticle_success() {
    setAuthenticated(user);
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void deleteArticle_success() {
    setAuthenticated(user);
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("title");

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void deleteArticle_throws_when_not_authorized() {
    setAuthenticated(user);
    User other = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), other.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle("title"));
  }

  @Test
  void deleteArticle_throws_when_not_authenticated() {
    setAnonymous();

    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("title"));
  }
}
