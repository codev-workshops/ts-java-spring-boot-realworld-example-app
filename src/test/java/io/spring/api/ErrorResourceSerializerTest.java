package io.spring.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.spring.api.exception.ErrorResource;
import io.spring.api.exception.ErrorResourceSerializer;
import io.spring.api.exception.FieldErrorResource;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  @Test
  public void should_serialize_empty_errors() throws Exception {
    ErrorResourceSerializer serializer = new ErrorResourceSerializer();
    ErrorResource errorResource = new ErrorResource(new ArrayList<>());

    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    serializer.serialize(errorResource, gen, provider);
    gen.flush();

    assertEquals("{\"errors\":{}}", writer.toString());
  }

  @Test
  public void should_serialize_errors_with_fields() throws Exception {
    ErrorResourceSerializer serializer = new ErrorResourceSerializer();
    FieldErrorResource fieldError =
        new FieldErrorResource("testObject", "email", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError));

    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    serializer.serialize(errorResource, gen, provider);
    gen.flush();

    String result = writer.toString();
    assertEquals("{\"errors\":{\"email\":[\"can't be empty\"]}}", result);
  }
}
