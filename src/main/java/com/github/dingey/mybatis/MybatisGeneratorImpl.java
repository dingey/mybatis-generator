package com.github.dingey.mybatis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MybatisGeneratorImpl implements MybatisGenerator {
    private final JdbcMeta jdbcMeta;
    private List<Table> tables = new ArrayList<>();
    private Class<?> extendsModel;
    private Class<?> extendsMapper;
    private Option option;

    MybatisGeneratorImpl(JdbcMeta jdbcMeta) {
        this.jdbcMeta = jdbcMeta;
    }

    @Override
    public MybatisGenerator table(String... tables) {
        if (tables == null || tables.length == 0) {
            this.tables = jdbcMeta.getAllTables();
        } else {
            for (String t : tables) {
                try {
                    this.tables.add(jdbcMeta.getTable(t));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (Table t : this.tables) {
            debug(t.toString());
        }
        return this;
    }

    @Override
    public <T> MybatisGenerator extendsModel(Class<T> extendsModel) {
        this.extendsModel = extendsModel;
        for (Class<?> a = extendsModel; a != Class.class && a != Object.class && a != null; a = extendsModel.getSuperclass()) {
            for (Field f : a.getDeclaredFields()) {
                extendsClassFields.add(f.getName());
            }
        }
        return this;
    }

    @Override
    public <T> MybatisGenerator extendsMapper(Class<T> mapperInterface) {
        this.extendsMapper = mapperInterface;
        for (Class<?> a = mapperInterface; a != Class.class && a != Object.class && a != null; a = mapperInterface.getSuperclass()) {
            for (Method m : a.getDeclaredMethods()) {
                extendsClassMethods.add(m.getName());
            }
        }
        return this;
    }

    @Override
    public MybatisGenerator generateModel(String packagePath) {
        modelPackage = packagePath;
        for (Table t : tables) {
            StringBuilder s = new StringBuilder("package ");
            s.append(packagePath).append(";\n\n");
            String cname = Str.of(t.getName().replaceFirst(getOption().getTablePrefix(), "")).camelCase().toString();
            String upper = StringUtil.firstUpper(cname);
            if (extendsModel != null) {
                s.append("import ").append(extendsModel.getName()).append(";\n");
            }
            HashSet<String> imports = new HashSet<>();
            for (Column c : t.getAllColumns()) {
                if (extendsClassFields.contains(Str.of(c.getColumnName()).camelCase().toString()))
                    continue;
                Class<?> java = TypeAdaptor.java(c);
                if (!java.getName().startsWith("java.lang")) {
                    imports.add(java.getName());
                }
            }
            if (!imports.isEmpty()) {
                for (String imp : imports) {
                    s.append("import ").append(imp).append(";\n");
                }
            }
            if (getOption().isSwagger()) {
                s.append("import io.swagger.annotations.ApiModel;\n");
                s.append("import io.swagger.annotations.ApiModelProperty;\n");
            }
            if (getOption().isValidateAnnotation()) {
                s.append("import javax.validation.constraints.*;\n");
            }
            if (getOption().isLombok()) {
                s.append("import lombok.Data;\n\n");
            }
            if (t.getComment() != null && !t.getComment().isEmpty()) {
                if (getOption().isSwagger()) {
                    s.append("@ApiModel(\"").append(t.getComment()).append("\")\n");
                } else {
                    s.append("/**\n * ").append(t.getComment()).append("\n */\n");
                }
            }
            if (getOption().isLombok()) {
                s.append("@Data\n");
            }
            s.append("public class ").append(upper);
            if (extendsModel != null) {
                s.append(" extends ").append(extendsModel.getSimpleName());
                if (extendsModel.getTypeParameters().length > 0) {
                    s.append("<").append(Str.of(t.getName()).camelCase().firstUpper()).append(">");
                }
            }
            s.append(" {\n");
            for (Column c : t.getAllColumns()) {
                if (extendsClassFields.contains(Str.of(c.getColumnName()).camelCase().toString()))
                    continue;
                if (c.getRemarks() != null && !c.getRemarks().isEmpty()) {
                    if (getOption().isSwagger()) {
                        s.append("    @ApiModelProperty(\"").append(c.getRemarks()).append("\")\n");
                    } else {
                        s.append("    /* ").append(c.getRemarks()).append(" */\n");
                    }
                }
                Class<?> java = TypeAdaptor.java(c);
                if (getOption().isValidateAnnotation()) {
                    if (java == String.class && !c.isPrimaryKey()) {
                        s.append("    @Size(max = ").append(c.getColumnSize()).append(")\n");
                    }
                    if (c.getNullable() == 0 && !c.isPrimaryKey()) {
                        s.append("    @NotNull\n");
                    }
                }
                s.append("    private ").append(java.getSimpleName()).append(" ").append(StringUtil.camelCase(c.getColumnName())).append(";\n");
            }
            if (!getOption().isLombok()) {
                for (Column c : t.getAllColumns()) {
                    if (extendsClassFields.contains(Str.of(c.getColumnName()).camelCase().toString()))
                        continue;
                    String java = TypeAdaptor.javaName(c);
                    String name = StringUtil.camelCase(c.getColumnName());
                    String upperName = StringUtil.firstUpper(name);
                    s.append("    ").append("public ").append(java).append(" get").append(upperName).append("() {\n");
                    s.append("        return ").append(name).append(";\n");
                    s.append("    }\n\n");
                    s.append("    ").append("public void set").append(upperName).append("(").append(java).append(" ").append(name).append(") {\n");
                    s.append("        this.").append(name).append(" = ").append(name).append(";\n");
                    s.append("    }\n\n");
                }
            }
            s.append("}");
            debug(s.toString());
            FileUtil.writeClassPath(packagePath, upper + ".java", s.toString());
        }
        return this;
    }

    private String modelPackage;
    private String mapperPackage;

    private final HashSet<String> extendsClassFields = new HashSet<>();

    private final HashSet<String> extendsClassMethods = new HashSet<>();

    @Override
    public MybatisGenerator generateMapper(String packagePath) {
        mapperPackage = packagePath;
        for (Table t : tables) {
            StringBuilder s = new StringBuilder();
            String name = Str.of(t.getName().replaceFirst(getOption().getTablePrefix(), "")).camelCase().toString();
            String upperName = Str.of(name).firstUpper().toString();
            if (t.getPrimaryKeys() == null || t.getPrimaryKeys().isEmpty()) {
                System.err.println("generateMapper error " + t.getName() + " has no define primary key.");
                continue;
            }
            Column key = t.getPrimaryKeys().get(0);
            String keyName = Str.of(key.getColumnName()).camelCase().toString();
            s.append("package ").append(packagePath).append(";\n\n");
            s.append("import ").append(modelPackage).append(".").append(upperName).append(";\n");
            if (!extendsClassMethods.contains("listByIds")) {
                s.append("import org.apache.ibatis.annotations.Param;\n");
                s.append("import java.util.Collection;\n");
            }
            if (!extendsClassMethods.contains("list") || !extendsClassMethods.contains("listByIds")) {
                s.append("import java.util.List;\n");
            }
            if (extendsMapper != null && !extendsMapper.getPackage().getName().equals(packagePath)) {
                s.append("import ").append(extendsMapper.getName()).append(";\n");
            }
            s.append("\n");
            s.append("public interface ").append(upperName).append("Mapper");
            if (extendsMapper != null) {
                s.append(" extends ").append(extendsMapper.getSimpleName());
                if (extendsMapper.getTypeParameters().length > 0) {
                    s.append("<").append(upperName).append(">");
                }
            }
            s.append(" {\n");
            if (key != null) {
                if (!extendsClassMethods.contains("delete")) {
                    if (getOption().isMethodAnnotation()) {
                        s.append("\n    /**\n");
                        s.append("     * 根据主键删除\n");
                        s.append("     *\n");
                        s.append("     * @param ").append(keyName).append(" 主键\n");
                        s.append("     * @return 影响行数\n");
                        s.append("     */");
                    }
                    s.append("\n    int delete(").append(TypeAdaptor.java(key).getSimpleName()).append(" ").append(keyName).append(");\n");
                }
                if (!extendsClassMethods.contains("get")) {
                    if (getOption().isMethodAnnotation()) {
                        s.append("\n    /**\n");
                        s.append("     * 根据主键查询\n");
                        s.append("     *\n");
                        s.append("     * @param ").append(keyName).append(" 主键\n");
                        s.append("     * @return 一条记录\n");
                        s.append("     */");
                    }
                    s.append("\n    int get(").append(TypeAdaptor.java(key).getSimpleName()).append(" ").append(keyName).append(");\n");
                }
            }
            if (!extendsClassMethods.contains("list")) {
                if (getOption().isMethodAnnotation()) {
                    s.append("\n    /**\n");
                    s.append("     * 查询列表\n");
                    s.append("     *\n");
                    s.append("     * @param ").append(name).append(" 参数\n");
                    s.append("     * @return 多条记录\n");
                    s.append("     */");
                }
                s.append("\n    List<").append(upperName).append("> list(").append(upperName).append(" ").append(name).append(");\n");
            }
            if (!extendsClassMethods.contains("listByIds")) {
                if (getOption().isMethodAnnotation()) {
                    s.append("\n    /**\n");
                    s.append("     *  根据主键批量查询\n");
                    s.append("     *\n");
                    s.append("     * @param ids 主键\n");
                    s.append("     * @return 多条记录\n");
                    s.append("     */");
                }
                s.append("\n    List<").append(upperName).append("> listByIds(@Param(\"ids\") Collection<").append(TypeAdaptor.javaName(key)).append("> ids);\n");
            }
            if (!extendsClassMethods.contains("insert")) {
                if (getOption().isMethodAnnotation()) {
                    s.append("\n    /**\n");
                    s.append("     * 新增\n");
                    s.append("     *\n");
                    s.append("     * @param ").append(name).append(" 参数\n");
                    s.append("     * @return 影响行数\n");
                    s.append("     */");
                }
                s.append("\n    int insert(").append(upperName).append(" ").append(name).append(");\n");
            }
            if (!extendsClassMethods.contains("insertSelective")) {
                if (getOption().isMethodAnnotation()) {
                    s.append("\n    /**\n");
                    s.append("     * 新增，忽略null列\n");
                    s.append("     *\n");
                    s.append("     * @param ").append(name).append(" 参数\n");
                    s.append("     * @return 影响行数\n");
                    s.append("     */");
                }
                s.append("\n    int insertSelective(").append(upperName).append(" ").append(name).append(");\n");
            }
            if (!extendsClassMethods.contains("update")) {
                if (getOption().isMethodAnnotation()) {
                    s.append("\n    /**\n");
                    s.append("     * 根据主键修改\n");
                    s.append("     *\n");
                    s.append("     * @param ").append(name).append(" 参数\n");
                    s.append("     * @return 影响行数\n");
                    s.append("     */");
                }
                s.append("\n    int update(").append(upperName).append(" ").append(name).append(");\n");
            }
            if (!extendsClassMethods.contains("updateSelective")) {
                if (getOption().isMethodAnnotation()) {
                    s.append("\n    /**\n");
                    s.append("     * 根据主键修改，忽略null列\n");
                    s.append("     *\n");
                    s.append("     * @param ").append(name).append(" 参数\n");
                    s.append("     * @return 影响行数\n");
                    s.append("     */");
                }
                s.append("\n    int updateSelective(").append(upperName).append(" ").append(name).append(");\n");
            }
            s.append("}");
            FileUtil.writeClassPath(packagePath, upperName + "Mapper.java", s.toString());
        }
        return this;
    }

    @Override
    public MybatisGenerator generateXml(String mapperXmlPath) {
        for (Table t : tables) {
            StringBuilder s = new StringBuilder();
            String name = Str.of(t.getName().replaceFirst(getOption().getTablePrefix(), "")).camelCase().toString();
            String uppername = StringUtil.firstUpper(name);
            if (t.getPrimaryKeys() == null || t.getPrimaryKeys().isEmpty()) {
                System.err.println("generateMapper error " + t.getName() + " has no define primary key.");
                continue;
            }
            Column key = t.getPrimaryKeys().get(0);
            String keyName = key == null ? "" : Str.of(key.getColumnName()).camelCase().toString();
            s.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            s.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >\n");
            s.append("<mapper namespace=\"").append(mapperPackage).append(".").append(uppername).append("Mapper\">\n");
            s.append("    <resultMap id=\"BaseResultMap\" type=\"").append(modelPackage).append(".").append(uppername).append("\">\n");
            if (key != null) {
                s.append("        <id property=\"").append(keyName).append("\" column=\"").append(key.getColumnName()).append("\"/>\n");
            }
            for (Column c : t.getColumns()) {
                s.append("        <result property=\"").append(Str.of(c.getColumnName()).camelCase()).append("\" column=\"").append(c.getColumnName()).append("\"/>\n");
            }
            s.append("    </resultMap>\n");
            s.append("    <sql id=\"Base_Column_List\">\n        ");
            for (Column c : t.getAllColumns()) {
                s.append(c.getColumnName()).append(", ");
            }
            s.deleteCharAt(s.length() - 2);
            s.append("\n    </sql>\n");
            if (getOption().isMethodAnnotation()) {
                s.append("    <!-- 根据主键删除 -->\n");
            }
            if (getOption().isDeleteMark()) {
                s.append("    <update id=\"delete\">\n");
                s.append("        update ").append(t.getName()).append(" set ").append(getOption().getDeleteMark()).append("=").append(getOption().getDeleteValue()).append(" where ").append(key.getColumnName()).append(" = #{").append(keyName).append("}\n");
                s.append("    </update>\n");
            } else {
                s.append("    <delete id=\"delete\">\n");
                s.append("        delete from ").append(t.getName()).append(" where ").append(key.getColumnName()).append(" = #{").append(keyName).append("}\n");
                s.append("    </delete>\n");
            }
            if (getOption().isMethodAnnotation()) {
                s.append("    <!-- 根据主键查询 -->\n");
            }
            s.append("    <select id=\"get\" resultMap=\"BaseResultMap\">\n");
            s.append("        select\n");
            s.append("        <include refid=\"Base_Column_List\"/>\n");
            s.append("        from ").append(t.getName()).append("\n");
            s.append("        where ").append(key.getColumnName()).append(" = #{").append(keyName).append("}\n");
            s.append("    </select>\n");
            if (getOption().isMethodAnnotation()) {
                s.append("    <!-- 查询 -->\n");
            }
            s.append("    <select id=\"list\" resultMap=\"BaseResultMap\">\n");
            s.append("        select\n");
            s.append("        <include refid=\"Base_Column_List\"/>\n");
            s.append("        from ").append(t.getName()).append("\n");
            s.append("        <where>\n");
            s.append("            <if test=\"").append(keyName).append(" != null\">\n");
            s.append("                and ").append(key.getColumnName()).append("=#{").append(keyName).append("}\n");
            s.append("            </if>\n");
            s.append("        </where>\n");
            s.append("        order by ").append(key.getColumnName()).append(" desc\n");
            s.append("    </select>\n");
            if (getOption().isMethodAnnotation()) {
                s.append("    <!-- 根据主键批量查询 -->\n");
            }
            s.append("    <select id=\"listByIds\" resultMap=\"BaseResultMap\">\n");
            s.append("        select\n");
            s.append("        <include refid=\"Base_Column_List\"/>\n");
            s.append("        from ").append(t.getName()).append(" where ").append(key.getColumnName()).append(" in\n");
            s.append("        <foreach collection=\"ids\" item=\"id\" separator=\",\" open=\"(\" close=\")\">\n");
            s.append("            #{id}\n");
            s.append("        </foreach>\n");
            s.append("    </select>\n");

            if (getOption().isMethodAnnotation()) {
                s.append("    <!-- 新增 -->\n");
            }
            s.append("    <insert id=\"insert\"");
            if (jdbcMeta.getDriver().contains("mysql")) {
                s.append(" useGeneratedKeys=\"true\">\n");
            } else {
                s.append(">\n");
            }
            if (jdbcMeta.getDriver().contains("oracle")) {
                s.append("        <selectKey resultType=\"").append(TypeAdaptor.java(key).getName()).append("\" keyProperty=\"").append(keyName).append("\" order=\"BEFORE\">\n");
                s.append("            select ").append(t.getName()).append("_SEQ.nextval from dual\n");
                s.append("        </selectKey>\n");
            }
            s.append("        insert into ").append(t.getName()).append("\n        (");
            for (Column c : t.getAllColumns()) {
                s.append(c.getColumnName()).append(", ");
            }
            s.deleteCharAt(s.length() - 2).append("\n        ) values (\n        ");
            for (Column c : t.getAllColumns()) {
                s.append("#{").append(Str.of(c.getColumnName()).camelCase()).append("}, ");
            }
            s.deleteCharAt(s.length() - 2).append(")\n");
            s.append("    </insert>\n");

            if (getOption().isMethodAnnotation()) {
                s.append("    <!-- 新增，忽略null列 -->\n");
            }
            s.append("    <insert id=\"insertSelective\"");
            if (jdbcMeta.getDriver().contains("mysql")) {
                s.append(" useGeneratedKeys=\"true\">\n");
            } else {
                s.append(">\n");
            }
            if (jdbcMeta.getDriver().contains("oracle")) {
                s.append("        <selectKey resultType=\"").append(TypeAdaptor.java(key).getName()).append("\" keyProperty=\"").append(keyName).append("\" order=\"BEFORE\">\n");
                s.append("            select ").append(t.getName()).append("_SEQ.nextval from dual\n");
                s.append("        </selectKey>\n");
            }
            s.append("        insert into ").append(t.getName()).append("\n");
            s.append("        <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
            List<Column> columns = t.getAllColumns();
            if (jdbcMeta.getDriver().contains("oracle")) {
                s.append("            ").append(key.getColumnName()).append(",\n");
                columns = t.getColumns();
            }
            for (Column c : columns) {
                s.append("            <if test=\"").append(Str.of(c.getColumnName()).camelCase()).append(" != null\">\n");
                s.append("                ").append(c.getColumnName()).append(",\n");
                s.append("            </if>\n");
            }
            s.append("        </trim>\n");
            s.append("        <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
            if (jdbcMeta.getDriver().contains("oracle")) {
                s.append("            #{").append(keyName).append("},\n");
            }
            for (Column c : columns) {
                String cname = Str.of(c.getColumnName()).camelCase().toString();
                s.append("            <if test=\"").append(cname).append(" != null\">\n");
                s.append("                #{").append(cname).append("},\n");
                s.append("            </if>\n");
            }
            s.append("        </trim>\n");
            s.append("    </insert>\n");

            if (getOption().isMethodAnnotation()) {
                s.append("    <!-- 根据主键修改 -->\n");
            }
            s.append("    <update id=\"update\">\n");
            s.append("        update ").append(t.getName()).append(" set\n");
            for (Column c : t.getColumns()) {
                s.append("        ").append(c.getColumnName()).append(" = #{").append(Str.of(c.getColumnName()).camelCase()).append("},\n");
            }
            s.deleteCharAt(s.length() - 2);
            s.append("        where ").append(key.getColumnName()).append(" = #{").append(keyName).append("}\n");
            s.append("    </update>\n");

            if (getOption().isMethodAnnotation()) {
                s.append("    <!-- 根据主键修改，忽略null列 -->\n");
            }
            s.append("    <update id=\"updateSelective\">\n");
            s.append("        update ").append(t.getName()).append("\n");
            s.append("        <set>\n");
            for (Column c : t.getColumns()) {
                String cname = Str.of(c.getColumnName()).camelCase().toString();
                s.append("            <if test=\"").append(cname).append(" != null\">\n");
                s.append("                ").append(c.getColumnName()).append(" = #{").append(cname).append("},\n");
                s.append("            </if>\n");
            }
            s.append("        </set>\n");
            s.append("        where ").append(key.getColumnName()).append(" = #{").append(keyName).append("}\n");
            s.append("    </update>\n");
            s.append("</mapper>");

            if (getOption().isResourcesXml()) {
                FileUtil.writeResourcePath(mapperXmlPath, uppername + "Mapper.xml", s.toString());
            } else {
                FileUtil.writeClassPath(mapperXmlPath, uppername + "Mapper.xml", s.toString());
            }
        }
        return this;
    }

    @Override
    public MybatisGenerator typeAdaptor(String type, Class<?> javaClass) {
        TypeAdaptor.adaptor(type, javaClass);
        return this;
    }

    @Override
    public MybatisGenerator opt(Option option) {
        this.option = option;
        return this;
    }

    private Option getOption() {
        return option == null ? new Option() : option;
    }

    private void debug(String s) {
        if (getOption().isDebuggable()) {
            try {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.debug("\n" + s);
            } catch (Exception e) {
                System.out.println(s);
            }
        }
    }

}
