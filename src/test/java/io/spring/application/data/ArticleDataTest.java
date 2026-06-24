package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  private ArticleData createSample() {
    ProfileData profile = new ProfileData("pid", "user1", "bio", "img", false);
    return new ArticleData(
        "id1",
        "slug1",
        "title1",
        "desc1",
        "body1",
        true,
        5,
        new DateTime(1000L),
        new DateTime(2000L),
        Arrays.asList("tag1", "tag2"),
        profile);
  }

  @Test
  public void testGettersAndSetters() {
    ArticleData data = new ArticleData();
    data.setId("id");
    data.setSlug("slug");
    data.setTitle("title");
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(true);
    data.setFavoritesCount(10);
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    List<String> tags = Arrays.asList("a", "b");
    data.setTagList(tags);
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    data.setProfileData(profile);

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(10, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(tags, data.getTagList());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  public void testEqualsWithSameReference() {
    ArticleData data = createSample();
    assertEquals(data, data);
  }

  @Test
  public void testEqualsWithNull() {
    ArticleData data = createSample();
    assertNotEquals(null, data);
  }

  @Test
  public void testEqualsWithDifferentType() {
    ArticleData data = createSample();
    assertNotEquals("string", data);
  }

  @Test
  public void testEqualsWithEqualObjects() {
    ProfileData profile1 = new ProfileData("pid", "user1", "bio", "img", false);
    ProfileData profile2 = new ProfileData("pid", "user1", "bio", "img", false);
    DateTime dt1 = new DateTime(1000L);
    DateTime dt2 = new DateTime(2000L);
    ArticleData data1 =
        new ArticleData(
            "id1", "slug1", "title1", "desc1", "body1", true, 5, dt1, dt2,
            Arrays.asList("tag1", "tag2"), profile1);
    ArticleData data2 =
        new ArticleData(
            "id1", "slug1", "title1", "desc1", "body1", true, 5, dt1, dt2,
            Arrays.asList("tag1", "tag2"), profile2);
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentId() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setId("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentSlug() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setSlug("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentTitle() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setTitle("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentDescription() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setDescription("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentBody() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setBody("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentFavorited() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setFavorited(false);
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentFavoritesCount() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setFavoritesCount(999);
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentCreatedAt() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setCreatedAt(new DateTime(9999L));
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentUpdatedAt() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setUpdatedAt(new DateTime(9999L));
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentTagList() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setTagList(Arrays.asList("other"));
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentProfileData() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    data2.setProfileData(new ProfileData("other", "other", "other", "other", true));
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithNullFields() {
    ArticleData data1 = new ArticleData();
    ArticleData data2 = new ArticleData();
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());

    data1.setId("id");
    assertNotEquals(data1, data2);
    assertNotEquals(data2, data1);
  }

  @Test
  public void testEqualsWithNullVsNonNullEachField() {
    ArticleData base = new ArticleData();
    ArticleData other = new ArticleData();

    base.setId(null);
    other.setId("x");
    assertNotEquals(base, other);

    base = new ArticleData();
    other = new ArticleData();
    base.setSlug(null);
    other.setSlug("x");
    assertNotEquals(base, other);

    base = new ArticleData();
    other = new ArticleData();
    base.setTitle(null);
    other.setTitle("x");
    assertNotEquals(base, other);

    base = new ArticleData();
    other = new ArticleData();
    base.setDescription(null);
    other.setDescription("x");
    assertNotEquals(base, other);

    base = new ArticleData();
    other = new ArticleData();
    base.setBody(null);
    other.setBody("x");
    assertNotEquals(base, other);

    base = new ArticleData();
    other = new ArticleData();
    base.setCreatedAt(null);
    other.setCreatedAt(new DateTime(1000L));
    assertNotEquals(base, other);

    base = new ArticleData();
    other = new ArticleData();
    base.setUpdatedAt(null);
    other.setUpdatedAt(new DateTime(1000L));
    assertNotEquals(base, other);

    base = new ArticleData();
    other = new ArticleData();
    base.setTagList(null);
    other.setTagList(Arrays.asList("x"));
    assertNotEquals(base, other);

    base = new ArticleData();
    other = new ArticleData();
    base.setProfileData(null);
    other.setProfileData(new ProfileData("id", "u", "b", "i", false));
    assertNotEquals(base, other);
  }

  @Test
  public void testHashCodeConsistency() {
    ArticleData data = createSample();
    int hash1 = data.hashCode();
    int hash2 = data.hashCode();
    assertEquals(hash1, hash2);
  }

  @Test
  public void testHashCodeWithNullFields() {
    ArticleData data = new ArticleData();
    int hash = data.hashCode();
    assertEquals(hash, data.hashCode());
  }

  @Test
  public void testToString() {
    ArticleData data = createSample();
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("ArticleData"));
    assertTrue(str.contains("id1"));
  }

  @Test
  public void testGetCursor() {
    DateTime updatedAt = new DateTime(5000L);
    ArticleData data = new ArticleData();
    data.setUpdatedAt(updatedAt);
    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
    assertEquals(updatedAt, cursor.getData());
  }

  @Test
  public void testCanEqual() {
    ArticleData data1 = createSample();
    ArticleData data2 = createSample();
    assertTrue(data1.canEqual(data2));
    assertFalse(data1.canEqual("string"));
  }
}
