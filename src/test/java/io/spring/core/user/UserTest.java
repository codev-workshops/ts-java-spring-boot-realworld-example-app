package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  void should_create_user() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "image.jpg");
    assertNotNull(user.getId());
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  void should_update_user_fields() {
    User user = new User("old@test.com", "olduser", "oldpass", "old bio", "old.jpg");
    user.update("new@test.com", "newuser", "newpass", "new bio", "new.jpg");
    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("new bio", user.getBio());
    assertEquals("new.jpg", user.getImage());
  }

  @Test
  void should_not_update_empty_fields() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "image.jpg");
    user.update("", "", "", "", "");
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  void should_not_update_null_fields() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "image.jpg");
    user.update(null, null, null, null, null);
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  void should_be_equal_by_id() {
    User user1 = new User("test@test.com", "testuser", "pass", "", "");
    User user2 = new User("other@test.com", "other", "pass", "", "");
    assertNotEquals(user1, user2);
    assertEquals(user1, user1);
  }

  @Test
  void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("user1", "user2");
    assertEquals("user1", relation.getUserId());
    assertEquals("user2", relation.getTargetId());
  }

  @Test
  void should_create_follow_relation_with_no_arg_constructor() {
    FollowRelation relation = new FollowRelation();
    assertNull(relation.getUserId());
    assertNull(relation.getTargetId());
  }

  @Test
  void should_test_follow_relation_equals_and_hashcode() {
    FollowRelation r1 = new FollowRelation("u1", "u2");
    FollowRelation r2 = new FollowRelation("u1", "u2");
    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
    assertNotEquals(r1, new FollowRelation("u1", "u3"));
    assertNotNull(r1.toString());
  }

  @Test
  void should_update_individual_fields() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "image.jpg");
    user.update("new@test.com", "", "", "", "");
    assertEquals("new@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
  }

  @Test
  void should_have_consistent_equals_hashcode() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    assertEquals(user.hashCode(), user.hashCode());
    assertNotNull(user.toString());
    assertNotEquals(user, null);
    assertNotEquals(user, "string");
  }
}
