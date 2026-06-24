package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

public class CommentTest {

  private void setId(Object obj, String id) throws Exception {
    Field field = obj.getClass().getDeclaredField("id");
    field.setAccessible(true);
    field.set(obj, id);
  }

  @Test
  public void constructor_setsFields() {
    Comment comment = new Comment("body", "user1", "article1");
    assertThat(comment.getBody(), is("body"));
    assertThat(comment.getUserId(), is("user1"));
    assertThat(comment.getArticleId(), is("article1"));
    assertNotNull(comment.getId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void equals_sameObject_returnsTrue() {
    Comment comment = new Comment("body", "user1", "article1");
    assertThat(comment.equals(comment), is(true));
  }

  @Test
  public void equals_null_returnsFalse() {
    Comment comment = new Comment("body", "user1", "article1");
    assertThat(comment.equals(null), is(false));
  }

  @Test
  public void equals_differentType_returnsFalse() {
    Comment comment = new Comment("body", "user1", "article1");
    assertThat(comment.equals("a string"), is(false));
  }

  @Test
  public void equals_differentId_returnsFalse() {
    Comment a = new Comment("body", "user1", "article1");
    Comment b = new Comment("body", "user1", "article1");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_sameId_returnsTrue() throws Exception {
    Comment a = new Comment("body", "user1", "article1");
    Comment b = new Comment("body2", "user2", "article2");
    setId(b, a.getId());
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_bothNullId_returnsTrue() {
    Comment a = new Comment();
    Comment b = new Comment();
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_thisNullIdOtherNot_returnsFalse() {
    Comment a = new Comment();
    Comment b = new Comment("body", "user1", "article1");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_thisNotNullOtherNull_returnsFalse() {
    Comment a = new Comment("body", "user1", "article1");
    Comment b = new Comment();
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void hashCode_nonNullId() {
    Comment comment = new Comment("body", "user1", "article1");
    int hash = comment.hashCode();
    assertThat(hash, is(comment.hashCode()));
  }

  @Test
  public void hashCode_nullId() {
    Comment comment = new Comment();
    int hash = comment.hashCode();
    assertThat(hash, is(comment.hashCode()));
  }

  @Test
  public void hashCode_sameId_sameHash() throws Exception {
    Comment a = new Comment("body", "user1", "article1");
    Comment b = new Comment("body2", "user2", "article2");
    setId(b, a.getId());
    assertThat(a.hashCode(), is(b.hashCode()));
  }
}
