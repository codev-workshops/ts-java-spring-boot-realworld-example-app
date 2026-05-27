package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  void constructor_sets_fields() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");

    assertEquals("user-id", relation.getUserId());
    assertEquals("target-id", relation.getTargetId());
  }

  @Test
  void equals_and_hashcode_work() {
    FollowRelation r1 = new FollowRelation("u1", "t1");
    FollowRelation r2 = new FollowRelation("u1", "t1");

    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
  }

  @Test
  void not_equal_when_different_fields() {
    FollowRelation r1 = new FollowRelation("u1", "t1");
    FollowRelation r2 = new FollowRelation("u1", "t2");

    assertNotEquals(r1, r2);
  }
}
