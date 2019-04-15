package com.github.dingey.mybatis;

import lombok.Data;

import java.util.List;

@Data
class Table {
	private String name;
	private List<Column> primaryKeys;
	private List<Column> columns;
	private List<Column> allColumns;
	private String comment;
}
