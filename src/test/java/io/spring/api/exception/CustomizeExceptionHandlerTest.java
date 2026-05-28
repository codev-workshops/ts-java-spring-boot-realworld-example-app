package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;
  private WebRequest request;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
    request = mock(WebRequest.class);
  }

  @Test
  void should_handle_invalid_request() {
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
    bindingResult.addError(new FieldError("object", "title", "can't be empty"));
    InvalidRequestException ex = new InvalidRequestException(bindingResult);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ex, request);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  void should_handle_invalid_authentication() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(ex, request);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  void should_handle_method_argument_not_valid() throws Exception {
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
    bindingResult.addError(new FieldError("object", "email", "should be an email"));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

    ResponseEntity<Object> response =
        handler.handleMethodArgumentNotValid(
            ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation_with_multi_segment_path() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("already exists");
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) descriptor);

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(ex, request);
    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation_with_single_segment_path() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("is invalid");
    Annotation annotation2 = mock(Annotation.class);
    when(annotation2.annotationType()).thenReturn((Class) Override.class);
    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation2);
    when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) descriptor);

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(ex, request);
    assertNotNull(result);
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }
}
