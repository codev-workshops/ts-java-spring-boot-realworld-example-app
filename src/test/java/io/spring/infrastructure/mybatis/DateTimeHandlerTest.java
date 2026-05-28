package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DateTimeHandlerTest {

  private DateTimeHandler handler;
  @Mock private PreparedStatement ps;
  @Mock private ResultSet rs;
  @Mock private CallableStatement cs;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    handler = new DateTimeHandler();
  }

  @Test
  void should_set_parameter_with_non_null_datetime() throws Exception {
    DateTime dt = new DateTime(2023, 6, 15, 12, 30, 0);
    handler.setParameter(ps, 1, dt, null);
    verify(ps).setTimestamp(eq(1), any(Timestamp.class), any());
  }

  @Test
  void should_set_parameter_with_null_datetime() throws Exception {
    handler.setParameter(ps, 1, null, null);
    verify(ps).setTimestamp(eq(1), isNull(), any());
  }

  @Test
  void should_get_result_by_column_name_non_null() throws Exception {
    Timestamp ts = new Timestamp(1686830000000L);
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, "created_at");
    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  void should_get_result_by_column_name_null() throws Exception {
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, "created_at");
    assertNull(result);
  }

  @Test
  void should_get_result_by_column_index_non_null() throws Exception {
    Timestamp ts = new Timestamp(1686830000000L);
    when(rs.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, 1);
    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  void should_get_result_by_column_index_null() throws Exception {
    when(rs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, 1);
    assertNull(result);
  }

  @Test
  void should_get_result_from_callable_statement_non_null() throws Exception {
    Timestamp ts = new Timestamp(1686830000000L);
    when(cs.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(cs, 1);
    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  void should_get_result_from_callable_statement_null() throws Exception {
    when(cs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(cs, 1);
    assertNull(result);
  }
}
