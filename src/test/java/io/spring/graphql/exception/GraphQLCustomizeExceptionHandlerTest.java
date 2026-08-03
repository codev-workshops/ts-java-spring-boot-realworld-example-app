package io.spring.graphql.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.constraints.NotBlank;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;

class GraphQLCustomizeExceptionHandlerTest {

  private final GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();

  @SuppressWarnings("unchecked")
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

  private DataFetcherExceptionHandlerParameters params(Throwable throwable) {
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
    when(environment.getExecutionStepInfo()).thenReturn(stepInfo);
    when(environment.getMergedField())
        .thenReturn(MergedField.newMergedField(Field.newField("field").build()).build());
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .exception(throwable)
        .dataFetchingEnvironment(environment)
        .build();
  }

  @Test
  void invalidAuthenticationIsMappedToUnauthenticatedError() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(params(new InvalidAuthenticationException()));

    assertThat(result.getErrors()).hasSize(1);
    GraphQLError error = result.getErrors().get(0);
    assertThat(error.getMessage()).isEqualTo("invalid email or password");
  }

  @Test
  void constraintViolationIsMappedToBadRequestWithExtensions() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation("createUser.arg0.email", "can't be empty"));
    DataFetcherExceptionHandlerResult result =
        handler.onException(params(new ConstraintViolationException(violations)));

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getExtensions()).containsKey("email");
  }

  @Test
  void otherExceptionsFallBackToDefaultHandler() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(params(new RuntimeException("boom")));

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage()).contains("boom");
  }

  @Test
  void getErrorsAsDataGroupsMessagesByField() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation("createUser.arg0.email", "can't be empty"));
    violations.add(violation("username", "already taken"));

    Error error =
        GraphQLCustomizeExceptionHandler.getErrorsAsData(
            new ConstraintViolationException(violations));

    assertThat(error.getMessage()).isEqualTo("BAD_REQUEST");
    List<ErrorItem> items = error.getErrors();
    assertThat(items).extracting(ErrorItem::getKey).containsExactlyInAnyOrder("email", "username");
  }

  @Test
  void getErrorsAsDataWithNoViolationsReturnsEmptyErrorList() {
    Error error =
        GraphQLCustomizeExceptionHandler.getErrorsAsData(
            new ConstraintViolationException(Collections.emptySet()));

    assertThat(error.getErrors()).isEmpty();
  }
}
