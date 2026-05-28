package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  void should_have_next_when_direction_next_and_has_extra() {
    TestNode node = new TestNode(new DateTime());
    CursorPager<TestNode> pager = new CursorPager<>(Arrays.asList(node), Direction.NEXT, true);
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_have_previous_when_direction_prev_and_has_extra() {
    TestNode node = new TestNode(new DateTime());
    CursorPager<TestNode> pager = new CursorPager<>(Arrays.asList(node), Direction.PREV, true);
    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_not_have_next_or_previous_when_no_extra() {
    TestNode node = new TestNode(new DateTime());
    CursorPager<TestNode> pager = new CursorPager<>(Arrays.asList(node), Direction.NEXT, false);
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_return_null_cursors_for_empty_data() {
    CursorPager<TestNode> pager = new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  void should_return_cursors_for_non_empty_data() {
    DateTime dt1 = new DateTime(2023, 1, 1, 0, 0);
    DateTime dt2 = new DateTime(2023, 6, 1, 0, 0);
    TestNode node1 = new TestNode(dt1);
    TestNode node2 = new TestNode(dt2);
    CursorPager<TestNode> pager =
        new CursorPager<>(Arrays.asList(node1, node2), Direction.NEXT, false);
    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
    assertEquals(dt1.getMillis(), ((DateTimeCursor) pager.getStartCursor()).getData().getMillis());
    assertEquals(dt2.getMillis(), ((DateTimeCursor) pager.getEndCursor()).getData().getMillis());
  }

  @Test
  void should_create_cursor_page_parameter_with_defaults() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();
    assertEquals(20, param.getLimit());
    assertEquals(21, param.getQueryLimit());
  }

  @Test
  void should_create_cursor_page_parameter_with_values() {
    DateTime cursor = new DateTime();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 50, Direction.NEXT);
    assertEquals(50, param.getLimit());
    assertEquals(51, param.getQueryLimit());
    assertEquals(cursor, param.getCursor());
    assertTrue(param.isNext());
  }

  @Test
  void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 2000, Direction.NEXT);
    assertEquals(1000, param.getLimit());
  }

  @Test
  void should_not_change_limit_for_zero_or_negative() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 0, Direction.NEXT);
    assertEquals(20, param.getLimit());

    CursorPageParameter<DateTime> param2 = new CursorPageParameter<>(null, -5, Direction.NEXT);
    assertEquals(20, param2.getLimit());
  }

  @Test
  void should_return_false_for_is_next_when_direction_prev() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.PREV);
    assertFalse(param.isNext());
  }

  @Test
  void should_parse_date_time_cursor() {
    DateTime dt = new DateTime(2023, 6, 15, 12, 30);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertEquals(String.valueOf(dt.getMillis()), cursor.toString());
  }

  @Test
  void should_parse_null_date_time_cursor() {
    assertNull(DateTimeCursor.parse(null));
  }

  @Test
  void should_parse_valid_cursor_string() {
    long millis = 1686830000000L;
    DateTime result = DateTimeCursor.parse(String.valueOf(millis));
    assertNotNull(result);
    assertEquals(millis, result.getMillis());
  }

  @Test
  void should_create_page_with_defaults() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_create_page_with_values() {
    Page page = new Page(5, 50);
    assertEquals(5, page.getOffset());
    assertEquals(50, page.getLimit());
  }

  @Test
  void should_cap_page_limit_at_100() {
    Page page = new Page(0, 200);
    assertEquals(100, page.getLimit());
  }

  @Test
  void should_not_set_negative_offset() {
    Page page = new Page(-1, 10);
    assertEquals(0, page.getOffset());
  }

  @Test
  void should_not_set_zero_or_negative_page_limit() {
    Page page = new Page(0, 0);
    assertEquals(20, page.getLimit());

    Page page2 = new Page(0, -5);
    assertEquals(20, page2.getLimit());
  }

  @Test
  void should_test_cursor_page_parameter_equals_and_hashcode() {
    CursorPageParameter<DateTime> p1 = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPageParameter<DateTime> p2 = new CursorPageParameter<>(null, 10, Direction.NEXT);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1, new CursorPageParameter<>(null, 20, Direction.NEXT));
    assertNotNull(p1.toString());
    assertEquals(p1.getDirection(), Direction.NEXT);
  }

  @Test
  void should_test_page_equals_and_hashcode() {
    Page p1 = new Page(0, 10);
    Page p2 = new Page(0, 10);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1, new Page(5, 10));
    assertNotNull(p1.toString());
  }

  @Test
  void should_test_page_cursor_toString() {
    DateTime dt = new DateTime(2023, 6, 15, 12, 30);
    DateTimeCursor cursor = new DateTimeCursor(dt);
    assertNotNull(cursor.toString());
    assertEquals(dt, cursor.getData());
  }

  @Test
  void should_get_pager_data() {
    TestNode node = new TestNode(new DateTime());
    CursorPager<TestNode> pager = new CursorPager<>(Arrays.asList(node), Direction.NEXT, false);
    assertEquals(1, pager.getData().size());
  }

  static class TestNode implements Node {
    private final DateTime dateTime;

    TestNode(DateTime dateTime) {
      this.dateTime = dateTime;
    }

    @Override
    public PageCursor getCursor() {
      return new DateTimeCursor(dateTime);
    }
  }
}
