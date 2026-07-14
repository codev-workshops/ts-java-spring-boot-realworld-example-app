package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation_with_ids() {
    FollowRelation relation = new FollowRelation("user1", "target1");
    assertEquals("user1", relation.getUserId());
    assertEquals("target1", relation.getTargetId());
  }

  @Test
  public void should_set_fields_on_no_args_constructor() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("user2");
    relation.setTargetId("target2");
    assertEquals("user2", relation.getUserId());
    assertEquals("target2", relation.getTargetId());
  }
}
