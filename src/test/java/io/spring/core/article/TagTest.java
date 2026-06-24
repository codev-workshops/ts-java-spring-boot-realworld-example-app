package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void constructor_setsNameAndId() {
    Tag tag = new Tag("java");
    assertThat(tag.getName(), is("java"));
    assertNotNull(tag.getId());
  }

  @Test
  public void equals_sameObject_returnsTrue() {
    Tag tag = new Tag("java");
    assertThat(tag.equals(tag), is(true));
  }

  @Test
  public void equals_null_returnsFalse() {
    Tag tag = new Tag("java");
    assertThat(tag.equals(null), is(false));
  }

  @Test
  public void equals_differentType_returnsFalse() {
    Tag tag = new Tag("java");
    assertThat(tag.equals("java"), is(false));
  }

  @Test
  public void equals_sameName_returnsTrue() {
    Tag a = new Tag("java");
    Tag b = new Tag("java");
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_differentName_returnsFalse() {
    Tag a = new Tag("java");
    Tag b = new Tag("python");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_bothNullName_returnsTrue() {
    Tag a = new Tag();
    Tag b = new Tag();
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_thisNullNameOtherNot_returnsFalse() {
    Tag a = new Tag();
    Tag b = new Tag("java");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_thisNotNullOtherNull_returnsFalse() {
    Tag a = new Tag("java");
    Tag b = new Tag();
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void hashCode_nonNullName() {
    Tag tag = new Tag("java");
    int hash = tag.hashCode();
    assertThat(hash, is(tag.hashCode()));
  }

  @Test
  public void hashCode_nullName() {
    Tag tag = new Tag();
    int hash = tag.hashCode();
    assertThat(hash, is(tag.hashCode()));
  }

  @Test
  public void hashCode_sameName_sameHash() {
    Tag a = new Tag("java");
    Tag b = new Tag("java");
    assertThat(a.hashCode(), is(b.hashCode()));
  }

  @Test
  public void toString_containsName() {
    Tag tag = new Tag("java");
    String str = tag.toString();
    assertThat(str.contains("java"), is(true));
  }

  @Test
  public void setters_work() {
    Tag tag = new Tag();
    tag.setId("test-id");
    tag.setName("python");
    assertThat(tag.getId(), is("test-id"));
    assertThat(tag.getName(), is("python"));
  }
}
