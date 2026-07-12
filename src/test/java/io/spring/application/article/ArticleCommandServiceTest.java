package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  public void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  public void should_create_article_from_param_and_creator_and_save() {
    User creator = new User("a@test.com", "a", "123", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("a new title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, creator);

    assertEquals("a new title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertEquals(creator.getId(), article.getUserId());
    assertEquals("a-new-title", article.getSlug());
    assertEquals(2, article.getTags().size());

    ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository).save(captor.capture());
    assertEquals(article.getId(), captor.getValue().getId());
  }

  @Test
  public void should_update_article_fields_and_save() {
    User creator = new User("a@test.com", "a", "123", "", "");
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("t"), creator.getId());
    UpdateArticleParam param = new UpdateArticleParam("new title", "new body", "new desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("new title", updated.getTitle());
    assertEquals("new desc", updated.getDescription());
    assertEquals("new body", updated.getBody());
    assertEquals("new-title", updated.getSlug());
    verify(articleRepository).save(article);
  }
}
