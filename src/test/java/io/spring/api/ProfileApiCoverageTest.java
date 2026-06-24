package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ProfileQueryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProfileApiCoverageTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private ProfileQueryService profileQueryService;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_return_404_when_unfollow_nonexistent_user() throws Exception {
    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/profiles/{username}/follow", "nonexistent")
        .then()
        .statusCode(404);
  }
}
