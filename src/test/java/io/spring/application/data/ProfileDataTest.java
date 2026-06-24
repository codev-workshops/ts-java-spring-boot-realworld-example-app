package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  private ProfileData createSample() {
    return new ProfileData("id1", "user1", "bio1", "img1", true);
  }

  @Test
  public void testGettersAndSetters() {
    ProfileData data = new ProfileData();
    data.setId("id");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("img");
    data.setFollowing(true);

    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  public void testEqualsWithSameReference() {
    ProfileData data = createSample();
    assertEquals(data, data);
  }

  @Test
  public void testEqualsWithNull() {
    ProfileData data = createSample();
    assertNotEquals(null, data);
  }

  @Test
  public void testEqualsWithDifferentType() {
    ProfileData data = createSample();
    assertNotEquals("string", data);
  }

  @Test
  public void testEqualsWithEqualObjects() {
    ProfileData data1 = new ProfileData("id1", "user1", "bio1", "img1", true);
    ProfileData data2 = new ProfileData("id1", "user1", "bio1", "img1", true);
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentId() {
    ProfileData data1 = createSample();
    ProfileData data2 = createSample();
    data2.setId("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentUsername() {
    ProfileData data1 = createSample();
    ProfileData data2 = createSample();
    data2.setUsername("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentBio() {
    ProfileData data1 = createSample();
    ProfileData data2 = createSample();
    data2.setBio("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentImage() {
    ProfileData data1 = createSample();
    ProfileData data2 = createSample();
    data2.setImage("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentFollowing() {
    ProfileData data1 = createSample();
    ProfileData data2 = createSample();
    data2.setFollowing(false);
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithNullFields() {
    ProfileData data1 = new ProfileData();
    ProfileData data2 = new ProfileData();
    assertEquals(data1, data2);

    data1.setId("id");
    assertNotEquals(data1, data2);
    assertNotEquals(data2, data1);
  }

  @Test
  public void testEqualsWithNullVsNonNull() {
    ProfileData base = new ProfileData();
    ProfileData other = new ProfileData();

    base.setId(null);
    other.setId("x");
    assertNotEquals(base, other);

    base = new ProfileData();
    other = new ProfileData();
    base.setUsername(null);
    other.setUsername("x");
    assertNotEquals(base, other);

    base = new ProfileData();
    other = new ProfileData();
    base.setBio(null);
    other.setBio("x");
    assertNotEquals(base, other);

    base = new ProfileData();
    other = new ProfileData();
    base.setImage(null);
    other.setImage("x");
    assertNotEquals(base, other);
  }

  @Test
  public void testHashCodeConsistency() {
    ProfileData data = createSample();
    assertEquals(data.hashCode(), data.hashCode());
  }

  @Test
  public void testHashCodeWithNullFields() {
    ProfileData data = new ProfileData();
    int hash = data.hashCode();
    assertEquals(hash, data.hashCode());
  }

  @Test
  public void testToString() {
    ProfileData data = createSample();
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("ProfileData"));
  }

  @Test
  public void testCanEqual() {
    ProfileData data1 = createSample();
    ProfileData data2 = createSample();
    assertTrue(data1.canEqual(data2));
    assertFalse(data1.canEqual("string"));
  }
}
