package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetchingEnvironment createMockDfe() {
    DataFetchingEnvironment mockDfe = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    when(mockDfe.getExecutionStepInfo()).thenReturn(stepInfo);
    return mockDfe;
  }

  @Test
  void testOnExceptionWithInvalidAuthenticationException() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(createMockDfe())
            .exception(ex)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testOnExceptionWithConstraintViolationException() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "must not be blank";
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
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn("must not be blank");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(createMockDfe())
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void testOnExceptionWithOtherException() {
    RuntimeException ex = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(createMockDfe())
            .exception(ex)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetErrorsAsData() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "must not be blank";
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
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn("must not be blank");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
    ErrorItem item = error.getErrors().get(0);
    assertEquals("email", item.getKey());
    assertTrue(item.getValue().contains("must not be blank"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetParamSingleLevel() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("fieldName");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "msg";
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
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn("msg");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    ErrorItem item = error.getErrors().get(0);
    assertEquals("fieldName", item.getKey());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testMultipleViolationsOnSameField() {
    ConstraintViolation<?> v1 = mock(ConstraintViolation.class);
    when(v1.getRootBeanClass()).thenReturn((Class) String.class);
    Path path1 = mock(Path.class);
    when(path1.toString()).thenReturn("create.param.email");
    when(v1.getPropertyPath()).thenReturn(path1);

    ConstraintDescriptor descriptor1 = mock(ConstraintDescriptor.class);
    when(descriptor1.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "msg1";
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
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    doReturn(descriptor1).when(v1).getConstraintDescriptor();
    when(v1.getMessage()).thenReturn("msg1");

    ConstraintViolation<?> v2 = mock(ConstraintViolation.class);
    when(v2.getRootBeanClass()).thenReturn((Class) String.class);
    Path path2 = mock(Path.class);
    when(path2.toString()).thenReturn("create.param.email");
    when(v2.getPropertyPath()).thenReturn(path2);

    ConstraintDescriptor descriptor2 = mock(ConstraintDescriptor.class);
    when(descriptor2.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "msg2";
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
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    doReturn(descriptor2).when(v2).getConstraintDescriptor();
    when(v2.getMessage()).thenReturn("msg2");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(v1);
    violations.add(v2);
    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
    ErrorItem item = error.getErrors().get(0);
    assertEquals("email", item.getKey());
    assertEquals(2, item.getValue().size());
  }
}
