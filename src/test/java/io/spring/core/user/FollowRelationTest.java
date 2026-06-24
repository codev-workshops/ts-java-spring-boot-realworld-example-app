package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  private void setField(Object obj, String fieldName, Object value) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }

  @Test
  public void constructor_setsFields() {
    FollowRelation rel = new FollowRelation("user1", "user2");
    assertThat(rel.getUserId(), is("user1"));
    assertThat(rel.getTargetId(), is("user2"));
  }

  @Test
  public void equals_sameObject_returnsTrue() {
    FollowRelation rel = new FollowRelation("user1", "user2");
    assertThat(rel.equals(rel), is(true));
  }

  @Test
  public void equals_null_returnsFalse() {
    FollowRelation rel = new FollowRelation("user1", "user2");
    assertThat(rel.equals(null), is(false));
  }

  @Test
  public void equals_differentType_returnsFalse() {
    FollowRelation rel = new FollowRelation("user1", "user2");
    assertThat(rel.equals("a string"), is(false));
  }

  @Test
  public void equals_sameFields_returnsTrue() {
    FollowRelation a = new FollowRelation("user1", "user2");
    FollowRelation b = new FollowRelation("user1", "user2");
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_differentUserId_returnsFalse() {
    FollowRelation a = new FollowRelation("user1", "user2");
    FollowRelation b = new FollowRelation("user3", "user2");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_differentTargetId_returnsFalse() {
    FollowRelation a = new FollowRelation("user1", "user2");
    FollowRelation b = new FollowRelation("user1", "user3");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_bothAllFieldsNull_returnsTrue() {
    FollowRelation a = new FollowRelation();
    FollowRelation b = new FollowRelation();
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_thisUserIdNullOtherNot_returnsFalse() {
    FollowRelation a = new FollowRelation();
    FollowRelation b = new FollowRelation("user1", "user2");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_thisNotNullUserIdOtherNull_returnsFalse() {
    FollowRelation a = new FollowRelation("user1", "user2");
    FollowRelation b = new FollowRelation();
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_sameUserIdBothNullTargetId_returnsTrue() throws Exception {
    FollowRelation a = new FollowRelation();
    FollowRelation b = new FollowRelation();
    setField(a, "userId", "user1");
    setField(b, "userId", "user1");
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_sameUserIdThisNullTargetId_returnsFalse() throws Exception {
    FollowRelation a = new FollowRelation();
    FollowRelation b = new FollowRelation("user1", "user2");
    setField(a, "userId", "user1");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_sameUserIdOtherNullTargetId_returnsFalse() throws Exception {
    FollowRelation a = new FollowRelation("user1", "user2");
    FollowRelation b = new FollowRelation();
    setField(b, "userId", "user1");
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void hashCode_nonNullFields() {
    FollowRelation rel = new FollowRelation("user1", "user2");
    int hash = rel.hashCode();
    assertThat(hash, is(rel.hashCode()));
  }

  @Test
  public void hashCode_nullFields() {
    FollowRelation rel = new FollowRelation();
    int hash = rel.hashCode();
    assertThat(hash, is(rel.hashCode()));
  }

  @Test
  public void hashCode_sameFields_sameHash() {
    FollowRelation a = new FollowRelation("user1", "user2");
    FollowRelation b = new FollowRelation("user1", "user2");
    assertThat(a.hashCode(), is(b.hashCode()));
  }

  @Test
  public void hashCode_oneFieldNull() throws Exception {
    FollowRelation rel = new FollowRelation();
    setField(rel, "userId", "user1");
    int hash = rel.hashCode();
    assertThat(hash, is(rel.hashCode()));
  }

  @Test
  public void toString_containsFields() {
    FollowRelation rel = new FollowRelation("user1", "user2");
    String str = rel.toString();
    assertThat(str.contains("user1"), is(true));
    assertThat(str.contains("user2"), is(true));
  }

  @Test
  public void setters_work() {
    FollowRelation rel = new FollowRelation();
    rel.setUserId("u1");
    rel.setTargetId("t1");
    assertThat(rel.getUserId(), is("u1"));
    assertThat(rel.getTargetId(), is("t1"));
  }
}
