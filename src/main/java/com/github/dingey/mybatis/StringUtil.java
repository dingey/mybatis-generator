package com.github.dingey.mybatis;

class StringUtil {
	public static String escape(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		} else {
			return s.trim().replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
		}
	}

	public static String firstLower(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	public static String firstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String camelCase(String column) {
		if (column.contains("_")) {
			String[] ss = column.toLowerCase().split("_");
			StringBuilder sb = new StringBuilder(ss[0]);
			if (ss.length > 1) {
				for (int i = 1; i < ss.length; i++) {
					sb.append(firstUpper(ss[i]));
				}
			}
			return sb.toString();
		} else {
			return column.toLowerCase();
		}
	}
}
