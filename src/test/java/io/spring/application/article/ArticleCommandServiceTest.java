package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("author@test.com", "author", "123", "", "");
  }

  @Test
  public void should_create_article_from_param_and_save() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("new title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    assertEquals("new title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertEquals("new-title", article.getSlug());
    assertEquals(user.getId(), article.getUserId());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_fields_and_save() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param =
        new UpdateArticleParam("updated title", "updated body", "updated desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("updated title", updated.getTitle());
    assertEquals("updated body", updated.getBody());
    assertEquals("updated desc", updated.getDescription());
    assertEquals("updated-title", updated.getSlug());
    ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository).save(captor.capture());
    assertEquals(article.getId(), captor.getValue().getId());
  }
}
