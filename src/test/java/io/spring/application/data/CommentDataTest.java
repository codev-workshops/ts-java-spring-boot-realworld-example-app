package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  private CommentData createSample() {
    ProfileData profile = new ProfileData("pid", "user1", "bio", "img", false);
    return new CommentData(
        "id1", "body1", "articleId1", new DateTime(1000L), new DateTime(2000L), profile);
  }

  @Test
  public void testGettersAndSetters() {
    CommentData data = new CommentData();
    data.setId("id");
    data.setBody("body");
    data.setArticleId("art");
    DateTime dt = new DateTime(1000L);
    data.setCreatedAt(dt);
    data.setUpdatedAt(dt);
    ProfileData profile = new ProfileData("pid", "u", "b", "i", false);
    data.setProfileData(profile);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("art", data.getArticleId());
    assertEquals(dt, data.getCreatedAt());
    assertEquals(dt, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  public void testEqualsWithSameReference() {
    CommentData data = createSample();
    assertEquals(data, data);
  }

  @Test
  public void testEqualsWithNull() {
    CommentData data = createSample();
    assertNotEquals(null, data);
  }

  @Test
  public void testEqualsWithDifferentType() {
    CommentData data = createSample();
    assertNotEquals("string", data);
  }

  @Test
  public void testEqualsWithEqualObjects() {
    ProfileData p1 = new ProfileData("pid", "user1", "bio", "img", false);
    ProfileData p2 = new ProfileData("pid", "user1", "bio", "img", false);
    DateTime dt1 = new DateTime(1000L);
    DateTime dt2 = new DateTime(2000L);
    CommentData data1 = new CommentData("id1", "body1", "articleId1", dt1, dt2, p1);
    CommentData data2 = new CommentData("id1", "body1", "articleId1", dt1, dt2, p2);
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentId() {
    CommentData data1 = createSample();
    CommentData data2 = createSample();
    data2.setId("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentBody() {
    CommentData data1 = createSample();
    CommentData data2 = createSample();
    data2.setBody("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentArticleId() {
    CommentData data1 = createSample();
    CommentData data2 = createSample();
    data2.setArticleId("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentCreatedAt() {
    CommentData data1 = createSample();
    CommentData data2 = createSample();
    data2.setCreatedAt(new DateTime(9999L));
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentUpdatedAt() {
    CommentData data1 = createSample();
    CommentData data2 = createSample();
    data2.setUpdatedAt(new DateTime(9999L));
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentProfileData() {
    CommentData data1 = createSample();
    CommentData data2 = createSample();
    data2.setProfileData(new ProfileData("other", "other", "o", "o", true));
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithNullFields() {
    CommentData data1 = new CommentData();
    CommentData data2 = new CommentData();
    assertEquals(data1, data2);

    data1.setId("id");
    assertNotEquals(data1, data2);
    assertNotEquals(data2, data1);
  }

  @Test
  public void testEqualsWithNullVsNonNull() {
    CommentData base = new CommentData();
    CommentData other = new CommentData();

    base.setId(null);
    other.setId("x");
    assertNotEquals(base, other);

    base = new CommentData();
    other = new CommentData();
    base.setBody(null);
    other.setBody("x");
    assertNotEquals(base, other);

    base = new CommentData();
    other = new CommentData();
    base.setArticleId(null);
    other.setArticleId("x");
    assertNotEquals(base, other);

    base = new CommentData();
    other = new CommentData();
    base.setCreatedAt(null);
    other.setCreatedAt(new DateTime(1000L));
    assertNotEquals(base, other);

    base = new CommentData();
    other = new CommentData();
    base.setUpdatedAt(null);
    other.setUpdatedAt(new DateTime(1000L));
    assertNotEquals(base, other);

    base = new CommentData();
    other = new CommentData();
    base.setProfileData(null);
    other.setProfileData(new ProfileData("id", "u", "b", "i", false));
    assertNotEquals(base, other);
  }

  @Test
  public void testHashCodeConsistency() {
    CommentData data = createSample();
    assertEquals(data.hashCode(), data.hashCode());
  }

  @Test
  public void testHashCodeWithNullFields() {
    CommentData data = new CommentData();
    int hash = data.hashCode();
    assertEquals(hash, data.hashCode());
  }

  @Test
  public void testToString() {
    CommentData data = createSample();
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("CommentData"));
  }

  @Test
  public void testGetCursor() {
    DateTime createdAt = new DateTime(3000L);
    CommentData data = new CommentData();
    data.setCreatedAt(createdAt);
    assertNotNull(data.getCursor());
    assertEquals(createdAt, data.getCursor().getData());
  }

  @Test
  public void testCanEqual() {
    CommentData data1 = createSample();
    CommentData data2 = createSample();
    assertTrue(data1.canEqual(data2));
    assertFalse(data1.canEqual("string"));
  }
}
