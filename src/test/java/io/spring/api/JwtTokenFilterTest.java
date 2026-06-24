package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.security.JwtTokenFilter;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtTokenFilterTest {

  private JwtTokenFilter filter;
  private JwtService jwtService;
  private UserRepository userRepository;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  public void setUp() {
    SecurityContextHolder.clearContext();
    filter = new JwtTokenFilter();
    jwtService = mock(JwtService.class);
    userRepository = mock(UserRepository.class);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    filterChain = mock(FilterChain.class);

    ReflectionTestUtils.setField(filter, "jwtService", jwtService);
    ReflectionTestUtils.setField(filter, "userRepository", userRepository);
  }

  @Test
  public void should_continue_filter_chain_when_no_authorization_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(jwtService, never()).getSubFromToken(any());
  }

  @Test
  public void should_continue_filter_chain_when_authorization_header_has_no_token()
      throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Token");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  public void should_continue_filter_chain_when_token_is_invalid() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Token invalidtoken");
    when(jwtService.getSubFromToken(eq("invalidtoken"))).thenReturn(Optional.empty());

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(userRepository, never()).findById(any());
  }

  @Test
  public void should_set_authentication_when_token_is_valid() throws Exception {
    User user = new User("test@test.com", "testuser", "123", "", "");
    when(request.getHeader("Authorization")).thenReturn("Token validtoken");
    when(jwtService.getSubFromToken(eq("validtoken"))).thenReturn(Optional.of(user.getId()));
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(userRepository).findById(eq(user.getId()));
  }
}
