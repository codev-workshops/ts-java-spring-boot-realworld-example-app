package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  public void testGetters() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id1", 5);
    assertEquals("id1", afc.getId());
    assertEquals(5, afc.getCount());
  }

  @Test
  public void testEqualsWithSameReference() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id1", 5);
    assertEquals(afc, afc);
  }

  @Test
  public void testEqualsWithNull() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id1", 5);
    assertNotEquals(null, afc);
  }

  @Test
  public void testEqualsWithDifferentType() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id1", 5);
    assertNotEquals("string", afc);
  }

  @Test
  public void testEqualsWithEqualObjects() {
    ArticleFavoriteCount afc1 = new ArticleFavoriteCount("id1", 5);
    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("id1", 5);
    assertEquals(afc1, afc2);
    assertEquals(afc1.hashCode(), afc2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentId() {
    ArticleFavoriteCount afc1 = new ArticleFavoriteCount("id1", 5);
    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("id2", 5);
    assertNotEquals(afc1, afc2);
  }

  @Test
  public void testEqualsWithDifferentCount() {
    ArticleFavoriteCount afc1 = new ArticleFavoriteCount("id1", 5);
    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("id1", 10);
    assertNotEquals(afc1, afc2);
  }

  @Test
  public void testEqualsWithNullId() {
    ArticleFavoriteCount afc1 = new ArticleFavoriteCount(null, 5);
    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("id1", 5);
    assertNotEquals(afc1, afc2);
    assertNotEquals(afc2, afc1);
  }

  @Test
  public void testEqualsWithNullCount() {
    ArticleFavoriteCount afc1 = new ArticleFavoriteCount("id1", null);
    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("id1", 5);
    assertNotEquals(afc1, afc2);
    assertNotEquals(afc2, afc1);
  }

  @Test
  public void testEqualsWithBothNulls() {
    ArticleFavoriteCount afc1 = new ArticleFavoriteCount(null, null);
    ArticleFavoriteCount afc2 = new ArticleFavoriteCount(null, null);
    assertEquals(afc1, afc2);
    assertEquals(afc1.hashCode(), afc2.hashCode());
  }

  @Test
  public void testHashCodeConsistency() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id1", 5);
    assertEquals(afc.hashCode(), afc.hashCode());
  }

  @Test
  public void testToString() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id1", 5);
    String str = afc.toString();
    assertNotNull(str);
    assertTrue(str.contains("ArticleFavoriteCount"));
    assertTrue(str.contains("id1"));
  }
}
