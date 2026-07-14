package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class DuplicatedArticleValidatorTest {

  @Mock private ArticleQueryService articleQueryService;

  private DuplicatedArticleValidator validator;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new DuplicatedArticleValidator();
    ReflectionTestUtils.setField(validator, "articleQueryService", articleQueryService);
  }

  @Test
  public void should_be_valid_when_no_article_with_slug() {
    when(articleQueryService.findBySlug("a-title", null)).thenReturn(Optional.empty());
    assertTrue(validator.isValid("a title", null));
  }

  @Test
  public void should_be_invalid_when_article_with_slug_exists() {
    when(articleQueryService.findBySlug("a-title", null))
        .thenReturn(Optional.of(new ArticleData()));
    assertFalse(validator.isValid("a title", null));
  }
}
