package io.spring.graphql;

import static io.spring.graphql.GraphQLTestFixtures.anonymous;
import static io.spring.graphql.GraphQLTestFixtures.authenticate;
import static io.spring.graphql.GraphQLTestFixtures.clearAuthentication;
import static io.spring.graphql.GraphQLTestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation mutation;
  private User currentUser;
  private Article article;

  @BeforeEach
  void setUp() {
    mutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    currentUser = user("jack");
    authenticate(currentUser);
    article = new Article("title", "desc", "body", Arrays.asList("java"), currentUser.getId());
  }

  @AfterEach
  void tearDown() {
    clearAuthentication();
  }

  @Test
  void createArticleReturnsPayloadWithArticleAsLocalContext() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), any(User.class)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = mutation.createArticle(input);

    assertThat(result.getLocalContext()).isEqualTo(article);
  }

  @Test
  void createArticleDefaultsTagListToEmpty() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("title").description("desc").body("body").build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), any(User.class)))
        .thenReturn(article);
    ArgumentCaptor<NewArticleParam> captor = ArgumentCaptor.forClass(NewArticleParam.class);

    mutation.createArticle(input);

    verify(articleCommandService).createArticle(captor.capture(), any(User.class));
    assertThat(captor.getValue().getTagList()).isEmpty();
  }

  @Test
  void createArticleThrowsWhenAnonymous() {
    anonymous();
    CreateArticleInput input = CreateArticleInput.newBuilder().title("title").build();

    assertThatThrownBy(() -> mutation.createArticle(input))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void updateArticleUpdatesOwnArticle() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(any(Article.class), any(UpdateArticleParam.class)))
        .thenReturn(article);
    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder().title("new").body("new body").description("new d").build();

    DataFetcherResult<ArticlePayload> result = mutation.updateArticle("slug", changes);

    assertThat(result.getLocalContext()).isEqualTo(article);
  }

  @Test
  void updateArticleThrowsWhenArticleMissing() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> mutation.updateArticle("slug", UpdateArticleInput.newBuilder().build()))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateArticleThrowsWhenNotAuthor() {
    Article othersArticle =
        new Article("t", "d", "b", Arrays.asList("java"), user("other").getId());
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(othersArticle));

    assertThatThrownBy(
            () -> mutation.updateArticle("slug", UpdateArticleInput.newBuilder().build()))
        .isInstanceOf(NoAuthorizationException.class);
  }

  @Test
  void favoriteArticleSavesFavorite() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = mutation.favoriteArticle("slug");

    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
    assertThat(result.getLocalContext()).isEqualTo(article);
  }

  @Test
  void favoriteArticleThrowsWhenArticleMissing() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.favoriteArticle("slug"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void unfavoriteArticleRemovesExistingFavorite() {
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), currentUser.getId());
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), currentUser.getId()))
        .thenReturn(Optional.of(favorite));

    mutation.unfavoriteArticle("slug");

    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  void unfavoriteArticleIsNoopWhenFavoriteMissing() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), currentUser.getId()))
        .thenReturn(Optional.empty());

    mutation.unfavoriteArticle("slug");

    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void deleteArticleRemovesOwnArticle() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(article));

    DeletionStatus status = mutation.deleteArticle("slug");

    verify(articleRepository).remove(article);
    assertThat(status.getSuccess()).isTrue();
  }

  @Test
  void deleteArticleThrowsWhenNotAuthor() {
    Article othersArticle =
        new Article("t", "d", "b", Arrays.asList("java"), user("other").getId());
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.of(othersArticle));

    assertThatThrownBy(() -> mutation.deleteArticle("slug"))
        .isInstanceOf(NoAuthorizationException.class);
  }

  @Test
  void deleteArticleThrowsWhenArticleMissing() {
    when(articleRepository.findBySlug("slug")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mutation.deleteArticle("slug"))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
