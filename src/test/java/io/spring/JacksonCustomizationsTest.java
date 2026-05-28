package io.spring;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.StringWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class JacksonCustomizationsTest {

  @Test
  void should_serialize_datetime() throws Exception {
    JacksonCustomizations customizations = new JacksonCustomizations();
    Module module = customizations.realWorldModules();
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(module);

    DateTime dateTime = new DateTime(2023, 6, 15, 12, 30, 0, DateTimeZone.UTC);
    String json = mapper.writeValueAsString(dateTime);
    assertNotNull(json);
    assertTrue(json.contains("2023"));
  }

  @Test
  void should_serialize_null_datetime() throws Exception {
    JacksonCustomizations.DateTimeSerializer serializer =
        new JacksonCustomizations.DateTimeSerializer();

    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator gen = mapper.getFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    serializer.serialize(null, gen, provider);
    gen.flush();
    assertEquals("null", writer.toString());
  }

  @Test
  void should_serialize_non_null_datetime() throws Exception {
    JacksonCustomizations.DateTimeSerializer serializer =
        new JacksonCustomizations.DateTimeSerializer();

    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator gen = mapper.getFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    DateTime dateTime = new DateTime(2023, 6, 15, 12, 30, 0, DateTimeZone.UTC);
    serializer.serialize(dateTime, gen, provider);
    gen.flush();
    assertTrue(writer.toString().contains("2023"));
  }
}
