package io.spring.application.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("jack@test.com", "jack", "123", "bio", "image");
  }

  @Test
  void createArticleSavesNewArticle() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("a new title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    verify(articleRepository).save(article);
    assertThat(article.getSlug()).isEqualTo("a-new-title");
    assertThat(article.getUserId()).isEqualTo(user.getId());
  }

  @Test
  void updateArticleAppliesChangesAndSaves() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), user.getId());

    Article updated =
        articleCommandService.updateArticle(
            article, new UpdateArticleParam("new title", "new body", "new desc"));

    verify(articleRepository).save(article);
    assertThat(updated.getTitle()).isEqualTo("new title");
    assertThat(updated.getBody()).isEqualTo("new body");
    assertThat(updated.getDescription()).isEqualTo("new desc");
    assertThat(updated.getSlug()).isEqualTo("new-title");
  }

  @Test
  void updateArticleIgnoresEmptyValues() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), user.getId());

    Article updated =
        articleCommandService.updateArticle(article, new UpdateArticleParam("", "", ""));

    assertThat(updated.getTitle()).isEqualTo("old title");
    assertThat(updated.getBody()).isEqualTo("old body");
    assertThat(updated.getDescription()).isEqualTo("old desc");
  }
}
