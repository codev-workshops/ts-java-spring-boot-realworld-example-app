package io.spring.graphql;

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
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleMutationTest extends GraphQLTestBase {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;
  @InjectMocks private ArticleMutation articleMutation;

  private Article article() {
    return new Article("title", "desc", "body", Arrays.asList("java"), CURRENT_USER.getId());
  }

  @Test
  void should_create_article_with_tags() {
    authenticate(CURRENT_USER);
    Article article = article();
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), any()))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    ArgumentCaptor<NewArticleParam> captor = ArgumentCaptor.forClass(NewArticleParam.class);
    verify(articleCommandService).createArticle(captor.capture(), any());
    assertThat(captor.getValue().getTagList()).containsExactly("java");
    assertThat(result.getLocalContext()).isSameAs(article);
  }

  @Test
  void should_create_article_with_empty_tag_list_when_null() {
    authenticate(CURRENT_USER);
    when(articleCommandService.createArticle(any(NewArticleParam.class), any()))
        .thenReturn(article());

    articleMutation.createArticle(
        CreateArticleInput.newBuilder().title("t").description("d").body("b").build());

    ArgumentCaptor<NewArticleParam> captor = ArgumentCaptor.forClass(NewArticleParam.class);
    verify(articleCommandService).createArticle(captor.capture(), any());
    assertThat(captor.getValue().getTagList()).isEmpty();
  }

  @Test
  void should_throw_when_create_without_authentication() {
    anonymous();
    assertThatThrownBy(() -> articleMutation.createArticle(CreateArticleInput.newBuilder().build()))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void should_update_article() {
    authenticate(CURRENT_USER);
    Article article = article();
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(any(Article.class), any(UpdateArticleParam.class)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(
            "title",
            UpdateArticleInput.newBuilder()
                .title("new title")
                .body("new body")
                .description("new desc")
                .build());

    assertThat(result.getLocalContext()).isSameAs(article);
  }

  @Test
  void should_throw_when_updating_article_of_another_user() {
    authenticate(CURRENT_USER);
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "other-user");
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    assertThatThrownBy(
            () -> articleMutation.updateArticle("title", UpdateArticleInput.newBuilder().build()))
        .isInstanceOf(NoAuthorizationException.class);
  }

  @Test
  void should_throw_when_updating_missing_article() {
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> articleMutation.updateArticle("missing", UpdateArticleInput.newBuilder().build()))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_favorite_article() {
    authenticate(CURRENT_USER);
    Article article = article();
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("title");

    verify(articleFavoriteRepository)
        .save(new ArticleFavorite(article.getId(), CURRENT_USER.getId()));
    assertThat(result.getLocalContext()).isSameAs(article);
  }

  @Test
  void should_throw_when_favoriting_missing_article() {
    authenticate(CURRENT_USER);
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.favoriteArticle("missing"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_unfavorite_article() {
    authenticate(CURRENT_USER);
    Article article = article();
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), CURRENT_USER.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), CURRENT_USER.getId()))
        .thenReturn(Optional.of(favorite));

    articleMutation.unfavoriteArticle("title");

    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  void should_ignore_unfavorite_when_favorite_absent() {
    authenticate(CURRENT_USER);
    Article article = article();
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), CURRENT_USER.getId()))
        .thenReturn(Optional.empty());

    articleMutation.unfavoriteArticle("title");

    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_own_article() {
    authenticate(CURRENT_USER);
    Article article = article();
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DeletionStatus status = articleMutation.deleteArticle("title");

    assertThat(status.getSuccess()).isTrue();
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_when_deleting_article_of_another_user() {
    authenticate(CURRENT_USER);
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "other-user");
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    assertThatThrownBy(() -> articleMutation.deleteArticle("title"))
        .isInstanceOf(NoAuthorizationException.class);
  }

  @Test
  void should_throw_when_deleting_missing_article() {
    authenticate(CURRENT_USER);
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.deleteArticle("missing"))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
