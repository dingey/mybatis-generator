package com.github.dingey.mybatis;

import java.util.HashMap;

class TypeAdaptor {
    private static HashMap<String, Class> javaType = new HashMap<>();

    static void adaptor(String typeName, Class<?> java) {
        javaType.put(typeName, java);
    }

    static Class<?> java(String typeName) {
        Class aClass = javaType.get(typeName);
        return aClass == null ? String.class : aClass;
    }

    static String javaName(Column c) {
        return java(c).getSimpleName();
    }

    static Class<?> java(Column column) {
        String k = column.getTypeName() + "(" + column.getColumnSize() + (column.getDecimalDigits() > 0 ? "," + column.getDecimalDigits() : "") + ")";
        if (javaType.containsKey(k)) {
            return java(k);
        } else if (javaType.containsKey(column.getTypeName())) {
            return java(column.getTypeName());
        } else {
            return JdbcJavaType.forCode(column.getDataType()).JAVA_TYPE;
        }
    }
}
