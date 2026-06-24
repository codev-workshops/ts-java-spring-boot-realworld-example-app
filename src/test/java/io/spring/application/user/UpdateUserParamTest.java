package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UpdateUserParamTest {

  @Test
  public void testBuilderWithAllFields() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("test@email.com")
            .password("pass")
            .username("user")
            .bio("bio")
            .image("img")
            .build();

    assertEquals("test@email.com", param.getEmail());
    assertEquals("pass", param.getPassword());
    assertEquals("user", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
  }

  @Test
  public void testBuilderDefaults() {
    UpdateUserParam param = UpdateUserParam.builder().build();
    assertEquals("", param.getEmail());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  public void testBuilderPartialFields() {
    UpdateUserParam param =
        UpdateUserParam.builder().email("test@email.com").username("user").build();
    assertEquals("test@email.com", param.getEmail());
    assertEquals("user", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  public void testNoArgsConstructor() {
    UpdateUserParam param = new UpdateUserParam();
    assertEquals("", param.getEmail());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  public void testAllArgsConstructor() {
    UpdateUserParam param = new UpdateUserParam("e@x.com", "pass", "user", "bio", "img");
    assertEquals("e@x.com", param.getEmail());
    assertEquals("pass", param.getPassword());
    assertEquals("user", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
  }

  @Test
  public void testBuilderToString() {
    String str = UpdateUserParam.builder().email("x").toString();
    assertNotNull(str);
  }
}
