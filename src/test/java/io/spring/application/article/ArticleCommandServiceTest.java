package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  void createArticle_creates_and_saves_article() {
    User creator = new User("test@email.com", "testuser", "pass", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("A test article")
            .body("Article body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, creator);

    assertNotNull(result);
    assertEquals("Test Article", result.getTitle());
    assertEquals("A test article", result.getDescription());
    assertEquals("Article body content", result.getBody());
    assertEquals(creator.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void updateArticle_updates_and_saves_article() {
    User creator = new User("test@email.com", "testuser", "pass", "", "");
    Article article =
        new Article("Old Title", "old desc", "old body", Collections.emptyList(), creator.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void createArticle_with_empty_tags() {
    User creator = new User("test@email.com", "testuser", "pass", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("desc")
            .body("body")
            .tagList(Collections.emptyList())
            .build();

    Article result = articleCommandService.createArticle(param, creator);

    assertNotNull(result);
    assertTrue(result.getTags().isEmpty());
  }
}
