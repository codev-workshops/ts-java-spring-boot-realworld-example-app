package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.application.CursorPager.Direction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class PaginationTest {

  private static class TestNode implements Node {
    private final DateTime time;

    TestNode(DateTime time) {
      this.time = time;
    }

    @Override
    public PageCursor getCursor() {
      return new DateTimeCursor(time);
    }
  }

  @Test
  public void page_should_default_and_clamp() {
    Page defaultPage = new Page();
    assertEquals(0, defaultPage.getOffset());
    assertEquals(20, defaultPage.getLimit());

    Page clamped = new Page(-5, 1000);
    assertEquals(0, clamped.getOffset());
    assertEquals(100, clamped.getLimit());

    Page normal = new Page(3, 30);
    assertEquals(3, normal.getOffset());
    assertEquals(30, normal.getLimit());
  }

  @Test
  public void cursor_page_parameter_should_clamp_limit_and_report_direction() {
    CursorPageParameter<String> next = new CursorPageParameter<>("c", 10, Direction.NEXT);
    assertEquals(10, next.getLimit());
    assertEquals(11, next.getQueryLimit());
    assertTrue(next.isNext());
    assertEquals("c", next.getCursor());

    CursorPageParameter<String> over = new CursorPageParameter<>("c", 5000, Direction.PREV);
    assertEquals(1000, over.getLimit());
    assertFalse(over.isNext());

    CursorPageParameter<String> nonPositive = new CursorPageParameter<>("c", 0, Direction.NEXT);
    assertEquals(20, nonPositive.getLimit());
  }

  @Test
  public void cursor_pager_next_direction() {
    DateTime now = new DateTime();
    List<TestNode> data = Arrays.asList(new TestNode(now), new TestNode(now.plusHours(1)));
    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, true);
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertEquals(data.get(0).getCursor().toString(), pager.getStartCursor().toString());
    assertEquals(data.get(1).getCursor().toString(), pager.getEndCursor().toString());
  }

  @Test
  public void cursor_pager_prev_direction() {
    DateTime now = new DateTime();
    List<TestNode> data = Arrays.asList(new TestNode(now));
    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.PREV, true);
    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  public void cursor_pager_empty_has_null_cursors() {
    CursorPager<TestNode> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  public void date_time_cursor_round_trip() {
    DateTime time = new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(time);
    assertEquals(String.valueOf(time.getMillis()), cursor.toString());
    assertEquals(time.getMillis(), cursor.getData().getMillis());

    DateTime parsed = DateTimeCursor.parse(cursor.toString());
    assertEquals(time.getMillis(), parsed.getMillis());
    assertNull(DateTimeCursor.parse(null));
  }
}
