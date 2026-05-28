package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  void should_create_article() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);
    assertNotNull(article);
    assertEquals("test-title", article.getSlug());
    assertEquals("Test Title", article.getTitle());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    Article article =
        new Article("Old Title", "old desc", "old body", Collections.emptyList(), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("New Title", "new body", "new desc");
    Article updated = articleCommandService.updateArticle(article, param);
    assertEquals("New Title", updated.getTitle());
    assertEquals("new body", updated.getBody());
    verify(articleRepository).save(article);
  }

  @Test
  void should_create_article_with_empty_tags() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Title")
            .description("desc")
            .body("body")
            .tagList(Collections.emptyList())
            .build();

    Article article = articleCommandService.createArticle(param, user);
    assertNotNull(article);
    assertTrue(article.getTags().isEmpty());
  }

  @Test
  void should_build_new_article_param() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("t")
            .description("d")
            .body("b")
            .tagList(Arrays.asList("tag1"))
            .build();
    assertEquals("t", param.getTitle());
    assertEquals("d", param.getDescription());
    assertEquals("b", param.getBody());
    assertEquals(1, param.getTagList().size());
  }

  @Test
  void should_build_update_article_param() {
    UpdateArticleParam param = new UpdateArticleParam("title", "body", "desc");
    assertEquals("title", param.getTitle());
    assertEquals("body", param.getBody());
    assertEquals("desc", param.getDescription());
  }
}
