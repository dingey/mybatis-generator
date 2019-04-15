package com.github.dingey.mybatis;

import lombok.Data;

@Data
class ImportKey {
	private String name;
	private String pkTableName;
	private String pkColumnName;
}
