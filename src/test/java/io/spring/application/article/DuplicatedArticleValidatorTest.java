package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DuplicatedArticleValidatorTest {

  @Mock private ArticleQueryService articleQueryService;

  @InjectMocks private DuplicatedArticleValidator validator;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void should_return_true_when_article_not_duplicated() {
    when(articleQueryService.findBySlug("new-title", null)).thenReturn(Optional.empty());
    assertTrue(validator.isValid("New Title", null));
  }

  @Test
  void should_return_false_when_article_is_duplicated() {
    ArticleData existing = mock(ArticleData.class);
    when(articleQueryService.findBySlug("existing-title", null)).thenReturn(Optional.of(existing));
    assertFalse(validator.isValid("Existing Title", null));
  }
}
