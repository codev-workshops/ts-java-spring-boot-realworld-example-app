package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void testConstructorAndGetData() {
    DateTime dt = new DateTime(5000L);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertEquals(dt, cursor.getData());
  }

  @Test
  public void testToString() {
    DateTime dt = new DateTime(5000L);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertEquals("5000", cursor.toString());
  }

  @Test
  public void testParseWithValidCursor() {
    DateTime result = DateTimeCursor.parse("5000");
    assertNotNull(result);
    assertEquals(5000L, result.getMillis());
    assertEquals(DateTimeZone.UTC, result.getZone());
  }

  @Test
  public void testParseWithNull() {
    DateTime result = DateTimeCursor.parse(null);
    assertNull(result);
  }
}
