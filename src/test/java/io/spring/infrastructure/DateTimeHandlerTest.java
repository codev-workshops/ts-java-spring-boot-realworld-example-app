package io.spring.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.infrastructure.mybatis.DateTimeHandler;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateTimeHandlerTest {

  private DateTimeHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  public void should_set_null_parameter_when_datetime_is_null() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);
    handler.setParameter(ps, 1, null, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(eq(1), eq(null), any(Calendar.class));
  }

  @Test
  public void should_set_timestamp_parameter_when_datetime_is_not_null() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);
    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);
    handler.setParameter(ps, 1, dateTime, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(eq(1), any(Timestamp.class), any(Calendar.class));
  }

  @Test
  public void should_return_null_from_result_set_by_column_name_when_timestamp_is_null()
      throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(anyString(), any(Calendar.class))).thenReturn(null);
    DateTime result = handler.getResult(rs, "created_at");
    assertNull(result);
  }

  @Test
  public void should_return_datetime_from_result_set_by_column_name() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    Timestamp timestamp = new Timestamp(1673780000000L);
    when(rs.getTimestamp(anyString(), any(Calendar.class))).thenReturn(timestamp);
    DateTime result = handler.getResult(rs, "created_at");
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  public void should_return_null_from_result_set_by_column_index_when_timestamp_is_null()
      throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(null);
    DateTime result = handler.getResult(rs, 1);
    assertNull(result);
  }

  @Test
  public void should_return_datetime_from_result_set_by_column_index() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    Timestamp timestamp = new Timestamp(1673780000000L);
    when(rs.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(timestamp);
    DateTime result = handler.getResult(rs, 1);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  public void should_return_null_from_callable_statement_when_timestamp_is_null() throws Exception {
    CallableStatement cs = mock(CallableStatement.class);
    when(cs.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(null);
    DateTime result = handler.getResult(cs, 1);
    assertNull(result);
  }

  @Test
  public void should_return_datetime_from_callable_statement() throws Exception {
    CallableStatement cs = mock(CallableStatement.class);
    Timestamp timestamp = new Timestamp(1673780000000L);
    when(cs.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(timestamp);
    DateTime result = handler.getResult(cs, 1);
    assertEquals(timestamp.getTime(), result.getMillis());
  }
}
