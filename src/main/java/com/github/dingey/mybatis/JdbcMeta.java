package com.github.dingey.mybatis;

import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.util.*;

@Getter
@Setter
public class JdbcMeta {
	private Connection conn;
	private String driver, url, username, password;

	protected JdbcMeta(String driver, String url, String username, String password) {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	private Connection getConn() throws SQLException {
		if (conn == null || conn.isClosed()) {
			try {
				Driver driver1 = (Driver) Class.forName(driver).newInstance();
				Properties props = new Properties();
				props.put("user", username);
				props.put("password", password);
				// Oracle 如果想要获取元数据 REMARKS 信息,需要加此参数
				props.put("remarksReporting", "true");
				// MySQL 标志位, 获取TABLE元数据 REMARKS 信息
				props.put("useInformationSchema", "true");
				DriverManager.registerDriver(driver1);
				conn = DriverManager.getConnection(url, props);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
				throw new RuntimeException("创建数据库连接失败" + e.getMessage(), e);
			}
		}
		return conn;
	}

	public List<Table> getAllTables() {
		List<Table> tables = new ArrayList<>();
		String catalog;
		ResultSet tablesResultSet = null;
		try {
			catalog = getConn().getCatalog();
			tablesResultSet = getConn().getMetaData().getTables(catalog, null, null, new String[]{"TABLE"});
			while (tablesResultSet.next()) {
				String tableName = tablesResultSet.getString("TABLE_NAME");
				tables.add(getTable(tableName));
			}
		} catch (SQLException e) {
			System.err.println("get all tables error:" + e.getMessage());
		} finally {
			try {
				if (tablesResultSet != null && !tablesResultSet.isClosed()) {
					tablesResultSet.close();
				}
			} catch (SQLException e) {
			}
		}
		return tables;
	}

	public Table getTable(String tableName) throws SQLException {
		String catalog = getConn().getCatalog();
		Table table = new Table();
		table.setName(tableName);
		// 主键
		ResultSet primaryKeyResultSet = getConn().getMetaData().getPrimaryKeys(null, null, tableName);
		Map<String, String> primaryKeyMap = new HashMap<>();
		while (primaryKeyResultSet.next()) {
			String primaryKeyColumnName = primaryKeyResultSet.getString("COLUMN_NAME");
			primaryKeyMap.put(primaryKeyColumnName, primaryKeyColumnName);
		}
		// 外键
		ResultSet foreignKeyResultSet = getConn().getMetaData().getImportedKeys(catalog, null, tableName);
		Map<String, ImportKey> foreignKeyMap = new HashMap<>();
		while (foreignKeyResultSet.next()) {
			ImportKey importKey = new ImportKey();
			importKey.setName(foreignKeyResultSet.getString("FKCOLUMN_NAME"));
			importKey.setPkTableName(foreignKeyResultSet.getString("PKTABLE_NAME"));
			importKey.setPkColumnName(foreignKeyResultSet.getString("PKCOLUMN_NAME"));
			foreignKeyMap.put(importKey.getName(), importKey);
		}
		// 提取表内的字段的名字和类型
		ResultSet columnSet = getConn().getMetaData().getColumns(null, "%", tableName, "%");
		List<Column> columns = new ArrayList<>();
		List<Column> primaryColumns = new ArrayList<>();
		List<Column> allColumns = new ArrayList<>();
		while (columnSet.next()) {
			Column c = new Column();
			c.setColumnName(columnSet.getString("COLUMN_NAME"));
			c.setTypeName(columnSet.getString("TYPE_NAME"));
			c.setNullable(columnSet.getInt("NULLABLE"));
			c.setRemarks(columnSet.getString("REMARKS"));
			c.setDecimalDigits(columnSet.getInt("DECIMAL_DIGITS"));
			c.setOrdinalPosition(columnSet.getInt("ORDINAL_POSITION"));
			c.setDataType(columnSet.getInt("DATA_TYPE"));
			c.setPrimaryKey(primaryKeyMap.get(c.getColumnName()) != null);
			c.setImportKey(foreignKeyMap.get(c.getColumnName()));
			c.setColumnSize(columnSet.getInt("COLUMN_SIZE"));
			if (c.isPrimaryKey()) {
				primaryColumns.add(c);
			} else {
				columns.add(c);
			}
			allColumns.add(c);
		}
		table.setColumns(columns);
		table.setPrimaryKeys(primaryColumns);
		table.setAllColumns(allColumns);
		table.setComment(getComment(tableName));
		return table;
	}

	private String getComment(String table) {
		String comment = "";
		try {
			if (driver.contains("mysql")) {
				comment = Comment.mysql(getConn(), table);
			} else if (driver.contains("oracle")) {
				comment = Comment.oracle(getConn(), table, null);
			}
		} catch (SQLException e) {
		}
		return StringUtil.escape(comment);
	}

	public void close() {
		try {
			if (conn != null && conn.isClosed()) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
