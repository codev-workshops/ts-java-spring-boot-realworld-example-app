package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  public void testDefaultConstructor() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();
    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
    assertNull(param.getDirection());
  }

  @Test
  public void testConstructorWithValidLimit() {
    DateTime cursor = new DateTime(1000L);
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 50, Direction.NEXT);
    assertEquals(50, param.getLimit());
    assertEquals(cursor, param.getCursor());
    assertEquals(Direction.NEXT, param.getDirection());
  }

  @Test
  public void testSetLimitExceedsMax() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 2000, Direction.NEXT);
    assertEquals(1000, param.getLimit());
  }

  @Test
  public void testSetLimitAtMax() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 1000, Direction.NEXT);
    assertEquals(1000, param.getLimit());
  }

  @Test
  public void testSetLimitZero() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 0, Direction.NEXT);
    assertEquals(20, param.getLimit());
  }

  @Test
  public void testSetLimitNegative() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, -5, Direction.NEXT);
    assertEquals(20, param.getLimit());
  }

  @Test
  public void testSetLimitOne() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 1, Direction.NEXT);
    assertEquals(1, param.getLimit());
  }

  @Test
  public void testIsNextWithNextDirection() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);
    assertTrue(param.isNext());
  }

  @Test
  public void testIsNextWithPrevDirection() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.PREV);
    assertFalse(param.isNext());
  }

  @Test
  public void testGetQueryLimit() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);
    assertEquals(21, param.getQueryLimit());
  }

  @Test
  public void testEqualsWithSameReference() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);
    assertEquals(param, param);
  }

  @Test
  public void testEqualsWithNull() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);
    assertNotEquals(null, param);
  }

  @Test
  public void testEqualsWithDifferentType() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);
    assertNotEquals("string", param);
  }

  @Test
  public void testEqualsWithEqualObjects() {
    DateTime cursor = new DateTime(1000L);
    CursorPageParameter<DateTime> param1 = new CursorPageParameter<>(cursor, 20, Direction.NEXT);
    CursorPageParameter<DateTime> param2 = new CursorPageParameter<>(cursor, 20, Direction.NEXT);
    assertEquals(param1, param2);
    assertEquals(param1.hashCode(), param2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentLimit() {
    CursorPageParameter<DateTime> param1 = new CursorPageParameter<>(null, 20, Direction.NEXT);
    CursorPageParameter<DateTime> param2 = new CursorPageParameter<>(null, 30, Direction.NEXT);
    assertNotEquals(param1, param2);
  }

  @Test
  public void testEqualsWithDifferentCursor() {
    CursorPageParameter<DateTime> param1 =
        new CursorPageParameter<>(new DateTime(1000L), 20, Direction.NEXT);
    CursorPageParameter<DateTime> param2 =
        new CursorPageParameter<>(new DateTime(2000L), 20, Direction.NEXT);
    assertNotEquals(param1, param2);
  }

  @Test
  public void testEqualsWithDifferentDirection() {
    CursorPageParameter<DateTime> param1 = new CursorPageParameter<>(null, 20, Direction.NEXT);
    CursorPageParameter<DateTime> param2 = new CursorPageParameter<>(null, 20, Direction.PREV);
    assertNotEquals(param1, param2);
  }

  @Test
  public void testEqualsWithNullCursorVsNonNull() {
    CursorPageParameter<DateTime> param1 = new CursorPageParameter<>(null, 20, Direction.NEXT);
    CursorPageParameter<DateTime> param2 =
        new CursorPageParameter<>(new DateTime(1000L), 20, Direction.NEXT);
    assertNotEquals(param1, param2);
    assertNotEquals(param2, param1);
  }

  @Test
  public void testEqualsWithNullDirectionVsNonNull() {
    CursorPageParameter<DateTime> param1 = new CursorPageParameter<>();
    param1.setDirection(null);
    CursorPageParameter<DateTime> param2 = new CursorPageParameter<>();
    param2.setDirection(Direction.NEXT);
    assertNotEquals(param1, param2);
    assertNotEquals(param2, param1);
  }

  @Test
  public void testHashCodeConsistency() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);
    assertEquals(param.hashCode(), param.hashCode());
  }

  @Test
  public void testHashCodeWithNullFields() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();
    int hash = param.hashCode();
    assertEquals(hash, param.hashCode());
  }

  @Test
  public void testToString() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);
    String str = param.toString();
    assertNotNull(str);
    assertTrue(str.contains("CursorPageParameter"));
  }

  @Test
  public void testCanEqual() {
    CursorPageParameter<DateTime> param1 = new CursorPageParameter<>(null, 20, Direction.NEXT);
    CursorPageParameter<DateTime> param2 = new CursorPageParameter<>(null, 20, Direction.NEXT);
    assertTrue(param1.canEqual(param2));
    assertFalse(param1.canEqual("string"));
  }

  @Test
  public void testSetDirection() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();
    param.setDirection(Direction.PREV);
    assertEquals(Direction.PREV, param.getDirection());
  }
}
