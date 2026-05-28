package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<?> createViolation(String pathStr, String message) {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);

    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) descriptor);

    return violation;
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable ex) {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(graphql.execution.ResultPath.rootPath());
    MergedField mergedField = MergedField.newMergedField().addField(new Field("test")).build();
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    when(dfe.getMergedField()).thenReturn(mergedField);
    when(dfe.getField()).thenReturn(new Field("test"));
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dfe)
        .exception(ex)
        .build();
  }

  @Test
  void should_handle_invalid_authentication_on_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = buildParams(ex);
    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_on_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("obj.param.email", "already exists"));
    ConstraintViolationException ex = new ConstraintViolationException(violations);
    DataFetcherExceptionHandlerParameters params = buildParams(ex);
    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_other_exceptions_on_exception() {
    RuntimeException ex = new RuntimeException("generic");
    DataFetcherExceptionHandlerParameters params = buildParams(ex);
    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("obj.param.email", "already exists"));
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(ex);
    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_handle_single_segment_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("email", "is invalid"));
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(ex);
    assertNotNull(error);
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  void should_handle_multi_segment_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("createUser.param.username", "already taken"));
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(ex);
    assertNotNull(error);
    assertEquals("username", error.getErrors().get(0).getKey());
  }
}
