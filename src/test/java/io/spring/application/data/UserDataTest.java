package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  private UserData createSample() {
    return new UserData("id1", "email1@test.com", "user1", "bio1", "img1");
  }

  @Test
  public void testGettersAndSetters() {
    UserData data = new UserData();
    data.setId("id");
    data.setEmail("email@test.com");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("img");

    assertEquals("id", data.getId());
    assertEquals("email@test.com", data.getEmail());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
  }

  @Test
  public void testEqualsWithSameReference() {
    UserData data = createSample();
    assertEquals(data, data);
  }

  @Test
  public void testEqualsWithNull() {
    UserData data = createSample();
    assertNotEquals(null, data);
  }

  @Test
  public void testEqualsWithDifferentType() {
    UserData data = createSample();
    assertNotEquals("string", data);
  }

  @Test
  public void testEqualsWithEqualObjects() {
    UserData data1 = new UserData("id1", "email1@test.com", "user1", "bio1", "img1");
    UserData data2 = new UserData("id1", "email1@test.com", "user1", "bio1", "img1");
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentId() {
    UserData data1 = createSample();
    UserData data2 = createSample();
    data2.setId("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentEmail() {
    UserData data1 = createSample();
    UserData data2 = createSample();
    data2.setEmail("different@test.com");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentUsername() {
    UserData data1 = createSample();
    UserData data2 = createSample();
    data2.setUsername("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentBio() {
    UserData data1 = createSample();
    UserData data2 = createSample();
    data2.setBio("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithDifferentImage() {
    UserData data1 = createSample();
    UserData data2 = createSample();
    data2.setImage("different");
    assertNotEquals(data1, data2);
  }

  @Test
  public void testEqualsWithNullFields() {
    UserData data1 = new UserData();
    UserData data2 = new UserData();
    assertEquals(data1, data2);

    data1.setId("id");
    assertNotEquals(data1, data2);
    assertNotEquals(data2, data1);
  }

  @Test
  public void testEqualsWithNullVsNonNull() {
    UserData base = new UserData();
    UserData other = new UserData();

    base.setId(null);
    other.setId("x");
    assertNotEquals(base, other);

    base = new UserData();
    other = new UserData();
    base.setEmail(null);
    other.setEmail("x");
    assertNotEquals(base, other);

    base = new UserData();
    other = new UserData();
    base.setUsername(null);
    other.setUsername("x");
    assertNotEquals(base, other);

    base = new UserData();
    other = new UserData();
    base.setBio(null);
    other.setBio("x");
    assertNotEquals(base, other);

    base = new UserData();
    other = new UserData();
    base.setImage(null);
    other.setImage("x");
    assertNotEquals(base, other);
  }

  @Test
  public void testHashCodeConsistency() {
    UserData data = createSample();
    assertEquals(data.hashCode(), data.hashCode());
  }

  @Test
  public void testHashCodeWithNullFields() {
    UserData data = new UserData();
    int hash = data.hashCode();
    assertEquals(hash, data.hashCode());
  }

  @Test
  public void testToString() {
    UserData data = createSample();
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("UserData"));
  }

  @Test
  public void testCanEqual() {
    UserData data1 = createSample();
    UserData data2 = createSample();
    assertTrue(data1.canEqual(data2));
    assertFalse(data1.canEqual("string"));
  }
}
