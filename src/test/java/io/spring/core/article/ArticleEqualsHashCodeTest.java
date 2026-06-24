package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ArticleEqualsHashCodeTest {

  private Article createArticle(String title) {
    return new Article(title, "desc", "body", Arrays.asList("java"), "user1");
  }

  private void setId(Object obj, String id) throws Exception {
    Field field = obj.getClass().getDeclaredField("id");
    field.setAccessible(true);
    field.set(obj, id);
  }

  @Test
  public void equals_sameObject_returnsTrue() {
    Article article = createArticle("title");
    assertThat(article.equals(article), is(true));
  }

  @Test
  public void equals_null_returnsFalse() {
    Article article = createArticle("title");
    assertThat(article.equals(null), is(false));
  }

  @Test
  public void equals_differentType_returnsFalse() {
    Article article = createArticle("title");
    assertThat(article.equals("a string"), is(false));
  }

  @Test
  public void equals_differentId_returnsFalse() {
    Article a = createArticle("title");
    Article b = createArticle("title");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_sameId_returnsTrue() throws Exception {
    Article a = createArticle("title");
    Article b = createArticle("title2");
    setId(b, a.getId());
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_bothNullId_returnsTrue() {
    Article a = new Article();
    Article b = new Article();
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_thisNullIdOtherNot_returnsFalse() {
    Article a = new Article();
    Article b = createArticle("title");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_thisNotNullOtherNull_returnsFalse() {
    Article a = createArticle("title");
    Article b = new Article();
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void hashCode_nonNullId() {
    Article article = createArticle("title");
    int hash = article.hashCode();
    assertThat(hash, is(article.hashCode()));
  }

  @Test
  public void hashCode_nullId() {
    Article article = new Article();
    int hash = article.hashCode();
    assertThat(hash, is(article.hashCode()));
  }

  @Test
  public void hashCode_sameId_sameHash() throws Exception {
    Article a = createArticle("title");
    Article b = createArticle("title2");
    setId(b, a.getId());
    assertThat(a.hashCode(), is(b.hashCode()));
  }

  @Test
  public void update_withNonNullValues() {
    Article article = createArticle("original");
    String origSlug = article.getSlug();
    article.update("new title", "new desc", "new body");
    assertThat(article.getTitle(), is("new title"));
    assertThat(article.getSlug(), is(not(origSlug)));
    assertThat(article.getDescription(), is("new desc"));
    assertThat(article.getBody(), is("new body"));
  }

  @Test
  public void update_withNullValues_noChange() {
    Article article = createArticle("original");
    String origTitle = article.getTitle();
    String origDesc = article.getDescription();
    String origBody = article.getBody();
    article.update(null, null, null);
    assertThat(article.getTitle(), is(origTitle));
    assertThat(article.getDescription(), is(origDesc));
    assertThat(article.getBody(), is(origBody));
  }

  @Test
  public void update_withEmptyStrings_noChange() {
    Article article = createArticle("original");
    String origTitle = article.getTitle();
    String origDesc = article.getDescription();
    String origBody = article.getBody();
    article.update("", "", "");
    assertThat(article.getTitle(), is(origTitle));
    assertThat(article.getDescription(), is(origDesc));
    assertThat(article.getBody(), is(origBody));
  }

  @Test
  public void update_mixedNullAndNonNull() {
    Article article = createArticle("original");
    String origDesc = article.getDescription();
    article.update("new title", null, "new body");
    assertThat(article.getTitle(), is("new title"));
    assertThat(article.getDescription(), is(origDesc));
    assertThat(article.getBody(), is("new body"));
  }
}
