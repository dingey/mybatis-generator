package com.github.dingey.mybatis;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

class Comment {
	static String oracle(Connection c, String table, String column) {
		ResultSet rs = null;
		try {
			if (column == null) {
				rs = c.createStatement().executeQuery("select * from user_tab_comments WHERE TABLE_NAME='" + table + "'");
			} else {
				rs = c.createStatement().executeQuery("select * from dba_col_comments WHERE TABLE_NAME='" + table + "' AND COLUMN_NAME='" + column + "'");
			}
			if (rs != null && rs.next()) {
				return rs.getString("COMMENTS");
			}
		} catch (SQLException e) {
		} finally {
			try {
				if (rs != null && rs.isClosed()) {
					rs.close();
				}
			} catch (SQLException e) {
			}
		}
		return "";
	}

	static String mysql(Connection c, String table) {
		String comment = "";
		ResultSet rs = null;
		try {
			rs = c.createStatement().executeQuery("SHOW CREATE TABLE " + table);
			if (rs != null && rs.next()) {
				String create = rs.getString(2);
				int index = create.indexOf("COMMENT='");
				if (index < 0) {
					return "";
				}
				comment = create.substring(index + 9);
				comment = comment.substring(0, comment.length() - 1);
				try {
					comment = new String(comment.getBytes("utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
		} finally {
			try {
				if (rs != null && rs.isClosed()) {
					rs.close();
				}
			} catch (SQLException e) {
			}
		}
		return comment;
	}
}
