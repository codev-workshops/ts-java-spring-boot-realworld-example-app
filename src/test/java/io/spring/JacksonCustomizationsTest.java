package io.spring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class JacksonCustomizationsTest {

  @Test
  public void should_serialize_null_datetime() throws Exception {
    JacksonCustomizations.DateTimeSerializer serializer =
        new JacksonCustomizations.DateTimeSerializer();
    JsonGenerator gen = mock(JsonGenerator.class);
    SerializerProvider provider = mock(SerializerProvider.class);

    serializer.serialize(null, gen, provider);

    verify(gen).writeNull();
  }

  @Test
  public void should_serialize_non_null_datetime() throws Exception {
    JacksonCustomizations.DateTimeSerializer serializer =
        new JacksonCustomizations.DateTimeSerializer();
    JsonGenerator gen = mock(JsonGenerator.class);
    SerializerProvider provider = mock(SerializerProvider.class);

    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);
    serializer.serialize(dateTime, gen, provider);

    verify(gen)
        .writeString(
            org.joda.time.format.ISODateTimeFormat.dateTime().withZoneUTC().print(dateTime));
  }
}
