package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateTimeHandlerTest {

  @Mock private PreparedStatement ps;
  @Mock private ResultSet rs;
  @Mock private CallableStatement cs;

  private DateTimeHandler handler;

  @BeforeEach
  void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  void setParameter_with_non_null_datetime() throws Exception {
    DateTime dt = new DateTime(1000000L);
    handler.setParameter(ps, 1, dt, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(eq(1), any(Timestamp.class), any());
  }

  @Test
  void setParameter_with_null_datetime() throws Exception {
    handler.setParameter(ps, 1, null, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(eq(1), isNull(), any());
  }

  @Test
  void getResult_by_column_name_returns_datetime() throws Exception {
    Timestamp ts = new Timestamp(1000000L);
    when(rs.getTimestamp(eq("col"), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, "col");

    assertNotNull(result);
    assertEquals(1000000L, result.getMillis());
  }

  @Test
  void getResult_by_column_name_returns_null() throws Exception {
    when(rs.getTimestamp(eq("col"), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, "col");

    assertNull(result);
  }

  @Test
  void getResult_by_column_index_returns_datetime() throws Exception {
    Timestamp ts = new Timestamp(2000000L);
    when(rs.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(rs, 1);

    assertNotNull(result);
    assertEquals(2000000L, result.getMillis());
  }

  @Test
  void getResult_by_column_index_returns_null() throws Exception {
    when(rs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, 1);

    assertNull(result);
  }

  @Test
  void getResult_from_callable_statement_returns_datetime() throws Exception {
    Timestamp ts = new Timestamp(3000000L);
    when(cs.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(cs, 1);

    assertNotNull(result);
    assertEquals(3000000L, result.getMillis());
  }

  @Test
  void getResult_from_callable_statement_returns_null() throws Exception {
    when(cs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(cs, 1);

    assertNull(result);
  }
}
