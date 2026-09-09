package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Write-side application service for articles.
 *
 * <p>Annotated with {@code @Validated}, so parameters marked {@code @Valid} are checked with bean
 * validation before the method body runs.
 */
@Service
@Validated
@AllArgsConstructor
public class ArticleCommandService {

  private ArticleRepository articleRepository;

  /**
   * Creates and persists a new article.
   *
   * @param newArticleParam title, description, body and tag list of the new article; validated with
   *     bean validation (non-blank fields and a duplicate-title check via {@code
   *     DuplicatedArticleConstraint})
   * @param creator the user who becomes the author of the article
   * @return the newly created and saved article
   * @throws javax.validation.ConstraintViolationException if {@code newArticleParam} fails
   *     validation
   */
  public Article createArticle(@Valid NewArticleParam newArticleParam, User creator) {
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            creator.getId());
    articleRepository.save(article);
    return article;
  }

  /**
   * Updates the title, description and body of an existing article and persists it. Blank values
   * leave the corresponding field unchanged; a new title also regenerates the slug.
   *
   * @param article the article to update; mutated in place
   * @param updateArticleParam the new title, description and body; validated with bean validation
   * @return the same {@code article} instance after the update
   * @throws javax.validation.ConstraintViolationException if {@code updateArticleParam} fails
   *     validation
   */
  public Article updateArticle(Article article, @Valid UpdateArticleParam updateArticleParam) {
    article.update(
        updateArticleParam.getTitle(),
        updateArticleParam.getDescription(),
        updateArticleParam.getBody());
    articleRepository.save(article);
    return article;
  }
}
