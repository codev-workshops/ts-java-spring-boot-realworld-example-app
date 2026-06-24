package io.spring.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.spring.api.exception.CustomizeExceptionHandler;
import io.spring.api.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  @Test
  public void should_handle_invalid_request_exception() {
    CustomizeExceptionHandler handler = new CustomizeExceptionHandler();

    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "testObject");
    errors.rejectValue(null, "invalid", "test error message");

    InvalidRequestException exception = new InvalidRequestException(errors);
    WebRequest webRequest = new ServletWebRequest(new MockHttpServletRequest());

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  public void should_handle_invalid_request_exception_with_field_error() {
    CustomizeExceptionHandler handler = new CustomizeExceptionHandler();

    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "testObject");
    errors.rejectValue(null, "NotBlank", "can't be empty");

    InvalidRequestException exception = new InvalidRequestException(errors);
    WebRequest webRequest = new ServletWebRequest(new MockHttpServletRequest());

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }
}
