package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_update_all_fields_when_non_empty() {
    User user = new User("old@test.com", "old", "oldpass", "oldbio", "oldimage");
    user.update("new@test.com", "new", "newpass", "newbio", "newimage");
    assertEquals("new@test.com", user.getEmail());
    assertEquals("new", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("newimage", user.getImage());
  }

  @Test
  public void should_keep_original_fields_when_update_values_empty() {
    User user = new User("old@test.com", "old", "oldpass", "oldbio", "oldimage");
    user.update("", "", "", "", "");
    assertEquals("old@test.com", user.getEmail());
    assertEquals("old", user.getUsername());
    assertEquals("oldpass", user.getPassword());
    assertEquals("oldbio", user.getBio());
    assertEquals("oldimage", user.getImage());
  }

  @Test
  public void should_keep_original_fields_when_update_values_null() {
    User user = new User("old@test.com", "old", "oldpass", "oldbio", "oldimage");
    user.update(null, null, null, null, null);
    assertEquals("old@test.com", user.getEmail());
    assertEquals("old", user.getUsername());
    assertEquals("oldpass", user.getPassword());
    assertEquals("oldbio", user.getBio());
    assertEquals("oldimage", user.getImage());
  }

  @Test
  public void should_update_only_provided_fields() {
    User user = new User("old@test.com", "old", "oldpass", "oldbio", "oldimage");
    user.update("new@test.com", "", "", "newbio", "");
    assertEquals("new@test.com", user.getEmail());
    assertEquals("old", user.getUsername());
    assertEquals("oldpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("oldimage", user.getImage());
  }
}
