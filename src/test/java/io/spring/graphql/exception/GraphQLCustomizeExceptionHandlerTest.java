package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;

public class GraphQLCustomizeExceptionHandlerTest {

  private DataFetchingEnvironment mockDfe() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    return dfe;
  }

  @Test
  void onException_handles_InvalidAuthenticationException() {
    GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockDfe())
            .exception(new InvalidAuthenticationException())
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void onException_handles_ConstraintViolationException() {
    GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();
    ConstraintViolationException cve = buildCVE("createUser.param.username", "can't be empty");
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockDfe())
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void onException_delegates_unknown_exception() {
    GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockDfe())
            .exception(new RuntimeException("unknown"))
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolationException buildCVE(String pathStr, String message) {
    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    doReturn((Class) Object.class).when(violation).getRootBeanClass();
    Path path = mock(Path.class);
    doReturn(pathStr).when(path).toString();
    doReturn(path).when(violation).getPropertyPath();
    doReturn(message).when(violation).getMessage();
    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    doReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }

              @Override
              public String message() {
                return "";
              }

              @Override
              public Class<?>[] groups() {
                return new Class[0];
              }

              @Override
              public Class<? extends javax.validation.Payload>[] payload() {
                return new Class[0];
              }
            })
        .when(descriptor)
        .getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    return new ConstraintViolationException(violations);
  }

  @Test
  void getErrorsAsData_returns_error_with_violations() {
    @SuppressWarnings("unchecked")
    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.registerParam.email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("can't be empty");

    @SuppressWarnings("unchecked")
    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }

              @Override
              public String message() {
                return "";
              }

              @Override
              public Class<?>[] groups() {
                return new Class[0];
              }

              @Override
              public Class<? extends javax.validation.Payload>[] payload() {
                return new Class[0];
              }
            });
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
    assertTrue(error.getErrors().get(0).getValue().contains("can't be empty"));
  }

  @Test
  void getErrorsAsData_handles_simple_path() {
    @SuppressWarnings("unchecked")
    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("invalid email");

    @SuppressWarnings("unchecked")
    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.Email() {
              @Override
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.Email.class;
              }

              @Override
              public String message() {
                return "";
              }

              @Override
              public Class<?>[] groups() {
                return new Class[0];
              }

              @Override
              public Class<? extends javax.validation.Payload>[] payload() {
                return new Class[0];
              }

              @Override
              public javax.validation.constraints.Pattern.Flag[] flags() {
                return new javax.validation.constraints.Pattern.Flag[0];
              }

              @Override
              public String regexp() {
                return ".*";
              }
            });
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals("email", error.getErrors().get(0).getKey());
  }
}
