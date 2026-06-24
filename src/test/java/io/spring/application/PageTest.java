package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  public void testDefaultConstructor() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  public void testConstructorWithValidValues() {
    Page page = new Page(5, 50);
    assertEquals(5, page.getOffset());
    assertEquals(50, page.getLimit());
  }

  @Test
  public void testSetLimitExceedsMax() {
    Page page = new Page(0, 200);
    assertEquals(100, page.getLimit());
  }

  @Test
  public void testSetLimitAtMax() {
    Page page = new Page(0, 100);
    assertEquals(100, page.getLimit());
  }

  @Test
  public void testSetLimitZero() {
    Page page = new Page(0, 0);
    assertEquals(20, page.getLimit());
  }

  @Test
  public void testSetLimitNegative() {
    Page page = new Page(0, -5);
    assertEquals(20, page.getLimit());
  }

  @Test
  public void testSetLimitOne() {
    Page page = new Page(0, 1);
    assertEquals(1, page.getLimit());
  }

  @Test
  public void testSetOffsetPositive() {
    Page page = new Page(10, 20);
    assertEquals(10, page.getOffset());
  }

  @Test
  public void testSetOffsetZero() {
    Page page = new Page(0, 20);
    assertEquals(0, page.getOffset());
  }

  @Test
  public void testSetOffsetNegative() {
    Page page = new Page(-5, 20);
    assertEquals(0, page.getOffset());
  }

  @Test
  public void testEqualsWithSameReference() {
    Page page = new Page(0, 20);
    assertEquals(page, page);
  }

  @Test
  public void testEqualsWithNull() {
    Page page = new Page(0, 20);
    assertNotEquals(null, page);
  }

  @Test
  public void testEqualsWithDifferentType() {
    Page page = new Page(0, 20);
    assertNotEquals("string", page);
  }

  @Test
  public void testEqualsWithEqualObjects() {
    Page page1 = new Page(5, 50);
    Page page2 = new Page(5, 50);
    assertEquals(page1, page2);
    assertEquals(page1.hashCode(), page2.hashCode());
  }

  @Test
  public void testEqualsWithDifferentOffset() {
    Page page1 = new Page(5, 20);
    Page page2 = new Page(10, 20);
    assertNotEquals(page1, page2);
  }

  @Test
  public void testEqualsWithDifferentLimit() {
    Page page1 = new Page(0, 20);
    Page page2 = new Page(0, 50);
    assertNotEquals(page1, page2);
  }

  @Test
  public void testHashCodeConsistency() {
    Page page = new Page(5, 50);
    assertEquals(page.hashCode(), page.hashCode());
  }

  @Test
  public void testToString() {
    Page page = new Page(5, 50);
    String str = page.toString();
    assertNotNull(str);
    assertTrue(str.contains("Page"));
  }

  @Test
  public void testCanEqual() {
    Page page1 = new Page(0, 20);
    Page page2 = new Page(0, 20);
    assertTrue(page1.canEqual(page2));
    assertFalse(page1.canEqual("string"));
  }
}
