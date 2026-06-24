package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

public class UserTest {

  private User createUser() {
    return new User("test@example.com", "testuser", "password", "bio", "image.jpg");
  }

  private void setId(Object obj, String id) throws Exception {
    Field field = obj.getClass().getDeclaredField("id");
    field.setAccessible(true);
    field.set(obj, id);
  }

  @Test
  public void constructor_setsFields() {
    User user = createUser();
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image.jpg"));
    assertNotNull(user.getId());
  }

  @Test
  public void equals_sameObject_returnsTrue() {
    User user = createUser();
    assertThat(user.equals(user), is(true));
  }

  @Test
  public void equals_null_returnsFalse() {
    User user = createUser();
    assertThat(user.equals(null), is(false));
  }

  @Test
  public void equals_differentType_returnsFalse() {
    User user = createUser();
    assertThat(user.equals("a string"), is(false));
  }

  @Test
  public void equals_differentId_returnsFalse() {
    User a = createUser();
    User b = createUser();
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_sameId_returnsTrue() throws Exception {
    User a = createUser();
    User b = createUser();
    setId(b, a.getId());
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_bothNullId_returnsTrue() {
    User a = new User();
    User b = new User();
    assertThat(a.equals(b), is(true));
  }

  @Test
  public void equals_thisNullIdOtherNot_returnsFalse() {
    User a = new User();
    User b = createUser();
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void equals_thisNotNullOtherNull_returnsFalse() {
    User a = createUser();
    User b = new User();
    assertThat(a.equals(b), is(false));
  }

  @Test
  public void hashCode_nonNullId() {
    User user = createUser();
    int hash = user.hashCode();
    assertThat(hash, is(user.hashCode()));
  }

  @Test
  public void hashCode_nullId() {
    User user = new User();
    int hash = user.hashCode();
    assertThat(hash, is(user.hashCode()));
  }

  @Test
  public void hashCode_sameId_sameHash() throws Exception {
    User a = createUser();
    User b = createUser();
    setId(b, a.getId());
    assertThat(a.hashCode(), is(b.hashCode()));
  }

  @Test
  public void update_allNonNullValues() {
    User user = createUser();
    user.update("new@example.com", "newuser", "newpass", "newbio", "newimage.jpg");
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("newbio"));
    assertThat(user.getImage(), is("newimage.jpg"));
  }

  @Test
  public void update_allNullValues_noChange() {
    User user = createUser();
    user.update(null, null, null, null, null);
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image.jpg"));
  }

  @Test
  public void update_allEmptyStrings_noChange() {
    User user = createUser();
    user.update("", "", "", "", "");
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image.jpg"));
  }

  @Test
  public void update_mixedNullAndNonNull() {
    User user = createUser();
    user.update("new@example.com", null, "newpass", "", "newimage.jpg");
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("newimage.jpg"));
  }

  @Test
  public void update_onlyEmail() {
    User user = createUser();
    user.update("new@example.com", null, null, null, null);
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("testuser"));
  }

  @Test
  public void update_onlyUsername() {
    User user = createUser();
    user.update(null, "newuser", null, null, null);
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getEmail(), is("test@example.com"));
  }

  @Test
  public void update_onlyPassword() {
    User user = createUser();
    user.update(null, null, "newpass", null, null);
    assertThat(user.getPassword(), is("newpass"));
  }

  @Test
  public void update_onlyBio() {
    User user = createUser();
    user.update(null, null, null, "newbio", null);
    assertThat(user.getBio(), is("newbio"));
  }

  @Test
  public void update_onlyImage() {
    User user = createUser();
    user.update(null, null, null, null, "newimage.jpg");
    assertThat(user.getImage(), is("newimage.jpg"));
  }
}
