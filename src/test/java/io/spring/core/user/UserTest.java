package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  void constructor_sets_all_fields_and_generates_uuid() {
    User user = new User("test@email.com", "testuser", "password", "bio text", "image.jpg");

    assertNotNull(user.getId());
    assertFalse(user.getId().isEmpty());
    assertEquals("test@email.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio text", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  void constructor_generates_unique_ids() {
    User user1 = new User("a@b.com", "u1", "p", "", "");
    User user2 = new User("c@d.com", "u2", "p", "", "");

    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  void update_with_non_empty_values_updates_fields() {
    User user = new User("old@email.com", "oldname", "oldpass", "oldbio", "oldimage");

    user.update("new@email.com", "newname", "newpass", "newbio", "newimage");

    assertEquals("new@email.com", user.getEmail());
    assertEquals("newname", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("newimage", user.getImage());
  }

  @Test
  void update_with_empty_values_leaves_fields_unchanged() {
    User user = new User("old@email.com", "oldname", "oldpass", "oldbio", "oldimage");

    user.update("", "", "", "", "");

    assertEquals("old@email.com", user.getEmail());
    assertEquals("oldname", user.getUsername());
    assertEquals("oldpass", user.getPassword());
    assertEquals("oldbio", user.getBio());
    assertEquals("oldimage", user.getImage());
  }

  @Test
  void update_with_null_values_leaves_fields_unchanged() {
    User user = new User("old@email.com", "oldname", "oldpass", "oldbio", "oldimage");

    user.update(null, null, null, null, null);

    assertEquals("old@email.com", user.getEmail());
    assertEquals("oldname", user.getUsername());
    assertEquals("oldpass", user.getPassword());
    assertEquals("oldbio", user.getBio());
    assertEquals("oldimage", user.getImage());
  }
}
