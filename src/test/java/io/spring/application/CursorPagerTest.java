package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private ArticleData createArticle(DateTime updatedAt) {
    ArticleData data = new ArticleData();
    data.setId("id");
    data.setUpdatedAt(updatedAt);
    return data;
  }

  @Test
  public void testNextDirectionWithExtra() {
    List<ArticleData> data = Arrays.asList(createArticle(new DateTime(1000L)));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, true);
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertEquals(data, pager.getData());
  }

  @Test
  public void testNextDirectionWithoutExtra() {
    List<ArticleData> data = Arrays.asList(createArticle(new DateTime(1000L)));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void testPrevDirectionWithExtra() {
    List<ArticleData> data = Arrays.asList(createArticle(new DateTime(1000L)));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, true);
    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  public void testPrevDirectionWithoutExtra() {
    List<ArticleData> data = Arrays.asList(createArticle(new DateTime(1000L)));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, false);
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void testGetStartCursorWithData() {
    DateTime dt = new DateTime(1000L);
    List<ArticleData> data = Arrays.asList(createArticle(dt));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);
    assertNotNull(pager.getStartCursor());
    assertEquals(dt, pager.getStartCursor().getData());
  }

  @Test
  public void testGetStartCursorEmpty() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    assertNull(pager.getStartCursor());
  }

  @Test
  public void testGetEndCursorWithData() {
    DateTime dt1 = new DateTime(1000L);
    DateTime dt2 = new DateTime(2000L);
    List<ArticleData> data = Arrays.asList(createArticle(dt1), createArticle(dt2));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);
    assertNotNull(pager.getEndCursor());
    assertEquals(dt2, pager.getEndCursor().getData());
  }

  @Test
  public void testGetEndCursorEmpty() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    assertNull(pager.getEndCursor());
  }
}
