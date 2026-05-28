package io.spring.api.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.spring.JacksonCustomizations;
import io.spring.api.UsersApi;
import io.spring.application.UserQueryService;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(UsersApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class WebSecurityConfigTest {

  @Autowired private MockMvc mvc;

  @MockBean private UserRepository userRepository;
  @MockBean private UserReadService userReadService;
  @MockBean private JwtService jwtService;
  @MockBean private UserQueryService userQueryService;
  @MockBean private UserService userService;

  @Test
  void should_return_401_for_protected_endpoint_without_auth() throws Exception {
    mvc.perform(put("/user").contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_not_return_401_for_post_users_login() throws Exception {
    MvcResult result =
        mvc.perform(
                post("/users/login")
                    .contentType("application/json")
                    .content("{\"user\":{\"email\":\"test@test.com\",\"password\":\"123456\"}}"))
            .andReturn();
    assertNotEquals(401, result.getResponse().getStatus());
  }
}
