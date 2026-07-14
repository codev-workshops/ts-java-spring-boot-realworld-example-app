package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
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
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ArticleMutationTest extends GraphQLTestBase {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;
  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("a@test.com", "alice", "123", "", "");
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
  }

  @Test
  public void should_create_article_with_tags() {
    setAuthenticatedUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);
    assertTrue(result.getLocalContext() == article);
  }

  @Test
  public void should_create_article_with_null_taglist() {
    setAuthenticatedUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("title").description("desc").body("body").build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);
    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);
    assertTrue(result.getLocalContext() == article);
  }

  @Test
  public void should_throw_when_create_unauthenticated() {
    setAnonymous();
    CreateArticleInput input = CreateArticleInput.newBuilder().title("t").build();
    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  public void should_update_article_when_authorized() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(article);
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder().title("new").body("b").description("d").build();

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("title", input);
    assertTrue(result.getLocalContext() == article);
  }

  @Test
  public void should_throw_when_update_not_found() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.empty());
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("new").build();
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.updateArticle("title", input));
  }

  @Test
  public void should_throw_when_update_not_authorized() {
    setAuthenticatedUser(user);
    User other = new User("o@test.com", "other", "123", "", "");
    Article othersArticle =
        new Article("title", "desc", "body", Arrays.asList("java"), other.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(othersArticle));
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("new").build();
    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.updateArticle("title", input));
  }

  @Test
  public void should_favorite_article() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("title");
    assertTrue(result.getLocalContext() == article);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  public void should_unfavorite_article() {
    setAuthenticatedUser(user);
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(favorite));
    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");
    assertTrue(result.getLocalContext() == article);
    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  public void should_delete_article_when_authorized() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    DeletionStatus status = articleMutation.deleteArticle("title");
    assertTrue(status.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  public void should_throw_when_delete_not_authorized() {
    setAuthenticatedUser(user);
    User other = new User("o@test.com", "other", "123", "", "");
    Article othersArticle =
        new Article("title", "desc", "body", Arrays.asList("java"), other.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(othersArticle));
    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle("title"));
  }
}
