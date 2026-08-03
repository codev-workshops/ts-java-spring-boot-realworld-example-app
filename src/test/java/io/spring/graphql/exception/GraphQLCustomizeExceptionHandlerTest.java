package io.spring.graphql.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

class GraphQLCustomizeExceptionHandlerTest {

  private final GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();

  static class Bean {
    @NotBlank(message = "can't be empty")
    private String email = "";
  }

  private ConstraintViolationException constraintViolationException() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<Bean>> violations = validator.validate(new Bean());
    return new ConstraintViolationException(violations);
  }

  private DataFetcherExceptionHandlerParameters parametersFor(Throwable throwable) {
    DataFetcherExceptionHandlerParameters parameters =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(parameters.getException()).thenReturn(throwable);
    when(parameters.getPath()).thenReturn(ResultPath.rootPath().segment("user"));
    return parameters;
  }

  @Test
  void should_map_invalid_authentication_to_unauthenticated_error() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(parametersFor(new InvalidAuthenticationException()));

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getExtensions().get("errorType"))
        .isEqualTo("UNAUTHENTICATED");
  }

  @Test
  void should_map_constraint_violation_to_bad_request_error() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(parametersFor(constraintViolationException()));

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getExtensions()).containsKey("email");
  }

  @Test
  void should_delegate_other_exceptions_to_default_handler() {
    DataFetcherExceptionHandlerParameters parameters =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(parameters.getException()).thenReturn(new RuntimeException("boom"));
    when(parameters.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(parameters);

    assertThat(result.getErrors()).isNotEmpty();
  }

  @Test
  void should_convert_violations_to_error_data() {
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(constraintViolationException());

    assertThat(error.getMessage()).isEqualTo("BAD_REQUEST");
    assertThat(error.getErrors()).hasSize(1);
    assertThat(error.getErrors().get(0).getKey()).isEqualTo("email");
    assertThat(error.getErrors().get(0).getValue()).containsExactly("can't be empty");
  }
}
