package io.spring.api.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.constraints.NotBlank;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

class CustomizeExceptionHandlerTest {

  private final CustomizeExceptionHandler handler = new CustomizeExceptionHandler();
  private final WebRequest request = mock(WebRequest.class);

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> violation(String propertyPath, String message) {
    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(NotBlank.class);

    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(annotation.annotationType()).thenReturn((Class) NotBlank.class);
    when(descriptor.getAnnotation()).thenReturn((Annotation) annotation);
    when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) descriptor);
    return violation;
  }

  private BindingResult bindingResultWithFieldError() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "article");
    bindingResult.addError(
        new FieldError(
            "article", "title", null, false, new String[] {"REQUIRED"}, null, "can't be empty"));
    return bindingResult;
  }

  @Test
  void handleInvalidRequestReturns422WithFieldErrors() {
    Errors errors = bindingResultWithFieldError();

    ResponseEntity<Object> response =
        handler.handleInvalidRequest(new InvalidRequestException(errors), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isInstanceOf(ErrorResource.class);
  }

  @Test
  void handleInvalidAuthenticationReturnsMessageBody() {
    ResponseEntity<Object> response =
        handler.handleInvalidAuthentication(new InvalidAuthenticationException(), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertThat(body).containsEntry("message", "invalid email or password");
  }

  @Test
  void handleConstraintViolationStripsMethodPrefixFromPropertyPath() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation("createUser.arg0.email", "can't be empty"));

    ErrorResource resource =
        handler.handleConstraintViolation(new ConstraintViolationException(violations), request);

    assertThat(resource.getFieldErrors()).hasSize(1);
    assertThat(resource.getFieldErrors().get(0).getField()).isEqualTo("email");
  }

  @Test
  void handleConstraintViolationKeepsSingleSegmentPropertyPath() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation("email", "can't be empty"));

    ErrorResource resource =
        handler.handleConstraintViolation(new ConstraintViolationException(violations), request);

    assertThat(resource.getFieldErrors().get(0).getField()).isEqualTo("email");
  }

  @Test
  void handleConstraintViolationWithNoViolationsReturnsEmptyErrors() {
    ErrorResource resource =
        handler.handleConstraintViolation(
            new ConstraintViolationException(Collections.emptySet()), request);

    assertThat(resource.getFieldErrors()).isEmpty();
  }

  @Test
  void handleMethodArgumentNotValidReturns422() throws Exception {
    BindingResult bindingResult = bindingResultWithFieldError();
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(
            new org.springframework.core.MethodParameter(
                CustomizeExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class), 0),
            bindingResult);

    ResponseEntity<Object> response =
        handler.handleMethodArgumentNotValid(
            exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    ErrorResource body = (ErrorResource) response.getBody();
    assertThat(body.getFieldErrors()).hasSize(1);
  }

  @Test
  void fieldErrorResourceExposesAllProperties() {
    FieldErrorResource resource = new FieldErrorResource("Article", "title", "REQUIRED", "empty");

    assertThat(Arrays.asList(resource.getResource(), resource.getField(), resource.getCode()))
        .containsExactly("Article", "title", "REQUIRED");
    assertThat(resource.getMessage()).isEqualTo("empty");
  }

  @SuppressWarnings("unused")
  private void dummy(String value) {}
}
