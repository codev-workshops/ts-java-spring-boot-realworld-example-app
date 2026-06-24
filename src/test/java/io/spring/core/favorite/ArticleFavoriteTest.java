package io.spring.core.favorite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  private void setField(Object obj, String fieldName, Object value) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }

  @Test
  public void constructor_setsFields() {
    ArticleFavorite fav = new ArticleFavorite("article1", "user1");
    assertThat(fav.getArticleId(), is("article1"));
    assertThat(fav.getUserId(), is("user1"));
  }

  @Test
  public void equals_sameObject_returnsTrue() {
    ArticleFavorite fav = new ArticleFavorite("article1", "user1");
    assertThat(fav.equals(fav), is(true));
  }

  @Test
  public void equals_null_returnsFalse() {
    ArticleFavorite fav = new ArticleFavorite("article1", "user1");
    assertThat(fav.equals(null), is(false));
  }

  @Test
  public void equals_differentType_returnsFalse() {
    ArticleFavorite fav = new ArticleFavorite("article1", "user1");
    assertThat(fav.equals("a string"), is(false));
  }

  @Test
  public void equals_sameFields_returnsTrue() {
    ArticleFavorite a = new ArticleFavorite("article1", "user1");
    ArticleFavorite b = new ArticleFavorite("article1", "user1");
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_differentArticleId_returnsFalse() {
    ArticleFavorite a = new ArticleFavorite("article1", "user1");
    ArticleFavorite b = new ArticleFavorite("article2", "user1");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_differentUserId_returnsFalse() {
    ArticleFavorite a = new ArticleFavorite("article1", "user1");
    ArticleFavorite b = new ArticleFavorite("article1", "user2");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_bothAllFieldsNull_returnsTrue() {
    ArticleFavorite a = new ArticleFavorite();
    ArticleFavorite b = new ArticleFavorite();
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_thisArticleIdNullOtherNot_returnsFalse() throws Exception {
    ArticleFavorite a = new ArticleFavorite();
    ArticleFavorite b = new ArticleFavorite("article1", "user1");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_thisNotNullArticleIdOtherNull_returnsFalse() throws Exception {
    ArticleFavorite a = new ArticleFavorite("article1", "user1");
    ArticleFavorite b = new ArticleFavorite();
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_sameArticleIdBothNullUserId_returnsTrue() throws Exception {
    ArticleFavorite a = new ArticleFavorite();
    ArticleFavorite b = new ArticleFavorite();
    setField(a, "articleId", "article1");
    setField(b, "articleId", "article1");
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_sameArticleIdThisNullUserId_returnsFalse() throws Exception {
    ArticleFavorite a = new ArticleFavorite();
    ArticleFavorite b = new ArticleFavorite("article1", "user1");
    setField(a, "articleId", "article1");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_sameArticleIdOtherNullUserId_returnsFalse() throws Exception {
    ArticleFavorite a = new ArticleFavorite("article1", "user1");
    ArticleFavorite b = new ArticleFavorite();
    setField(b, "articleId", "article1");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void hashCode_nonNullFields() {
    ArticleFavorite fav = new ArticleFavorite("article1", "user1");
    int hash = fav.hashCode();
    assertThat(hash, is(fav.hashCode()));
  }

  @Test
  public void hashCode_nullFields() {
    ArticleFavorite fav = new ArticleFavorite();
    int hash = fav.hashCode();
    assertThat(hash, is(fav.hashCode()));
  }

  @Test
  public void hashCode_sameFields_sameHash() {
    ArticleFavorite a = new ArticleFavorite("article1", "user1");
    ArticleFavorite b = new ArticleFavorite("article1", "user1");
    assertThat(a.hashCode(), is(b.hashCode()));
  }

  @Test
  public void hashCode_oneFieldNull() throws Exception {
    ArticleFavorite fav = new ArticleFavorite();
    setField(fav, "articleId", "article1");
    int hash = fav.hashCode();
    assertThat(hash, is(fav.hashCode()));
  }
}
