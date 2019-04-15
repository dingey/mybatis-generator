package com.github.dingey.mybatis;

public class MybatisGeneratorBuilder {
	private MybatisGeneratorBuilder() {
	}

	public static MybatisGenerator build(String driverClassName, String url, String username, String password) {
		JdbcMeta jdbcMeta = new JdbcMeta(driverClassName, url, username, password);
		return new MybatisGeneratorImpl(jdbcMeta);
	}
}
