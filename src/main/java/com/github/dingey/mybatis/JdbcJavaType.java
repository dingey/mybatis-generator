package com.github.dingey.mybatis;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
enum JdbcJavaType {
	ARRAY(2003, Array.class),
	BIT(-7, Boolean.class),
	TINYINT(-6, Byte.class),
	SMALLINT(5, Short.class),
	INTEGER(4, Integer.class),
	BIGINT(-5, Long.class),
	FLOAT(6, Float.class),
	REAL(7, Float.class),
	DOUBLE(8, Double.class),
	NUMERIC(2, BigDecimal.class),
	DECIMAL(3, BigDecimal.class),
	CHAR(1, String.class),
	VARCHAR(12, String.class),
	LONGVARCHAR(-1, String.class),
	DATE(91, java.sql.Date.class),
	TIME(92, java.sql.Time.class),
	TIMESTAMP(93, java.sql.Timestamp.class),
	BINARY(-2, byte[].class),
	VARBINARY(-3, byte[].class),
	LONGVARBINARY(-4, byte[].class),
	NULL(0, Object.class),
	OTHER(1111, String.class),
	BLOB(2004, Blob.class),
	CLOB(2005, Clob.class),
	BOOLEAN(16, boolean.class),
	CURSOR(-10, String.class),
	UNDEFINED(-2147482648, String.class),
	NVARCHAR(-9, String.class),
	NCHAR(-15, String.class),
	NCLOB(2011, String.class),
	STRUCT(2002, String.class),
	JAVA_OBJECT(2000, Object.class),
	DISTINCT(2001, String.class),
	REF(2006, String.class),
	DATALINK(70, String.class),
	ROWID(-8, String.class),
	LONGNVARCHAR(-16, String.class),
	SQLXML(2009, String.class),
	DATETIMEOFFSET(-155, String.class),
	TIME_WITH_TIMEZONE(2013, java.util.Date.class),
	TIMESTAMP_WITH_TIMEZONE(2014, java.util.Date.class);

	public final int TYPE_CODE;
	public final Class JAVA_TYPE;

	private static Map<Integer, JdbcJavaType> codeLookup = new HashMap<>();

	JdbcJavaType(int code, Class javaType) {
		this.TYPE_CODE = code;
		this.JAVA_TYPE = javaType;
	}

	public static JdbcJavaType forCode(int code) {
		return (JdbcJavaType) codeLookup.get(code);
	}

	static {
		for (JdbcJavaType type : values()) {
			codeLookup.put(type.TYPE_CODE, type);
		}
	}
}
