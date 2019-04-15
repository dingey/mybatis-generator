package com.github.dingey.mybatis;

import lombok.Data;

@Data
class Column {
	private int charOctetLength;
	private String typeName;
	private String isNullable;
	private int nullable;
	private int numPrecRadix;
	private int decimalDigits;
	private int sqlDataType;
	private String columnName;
	private String tableName;
	private int ordinalPosition;
	private String tableSchem;
	private int columnSize;
	private int dataType;
	private int bufferLength;
	private int sqlDatetimeSub;
	private String remarks;

	private boolean primaryKey;
	private ImportKey importKey;
}
