package com.github.dingey.mybatis;

import lombok.Getter;

/**
 * 生成配置选项
 */
@Getter
@SuppressWarnings("unused")
public class Option {

    private boolean methodAnnotation = true;

    private boolean debuggable = false;

    private Boolean lombok;

    private Boolean swagger;

    private Boolean validateAnnotation;

    private boolean resourcesXml = true;

    private String deleteMark;
    private String deleteValue;
    private String tablePrefix = "";

    public Boolean isValidateAnnotation() {
        if (validateAnnotation == null) {
            try {
                Class.forName("javax.validation.constraints.Size");
                validateAnnotation = true;
            } catch (ClassNotFoundException e) {
                validateAnnotation = false;
            }
        }
        return validateAnnotation;
    }

    Boolean isLombok() {
        if (lombok == null) {
            try {
                Class.forName("lombok.Data");
                lombok = true;
            } catch (ClassNotFoundException e) {
                lombok = false;
            }
        }
        return lombok;
    }

    Boolean isSwagger() {
        if (swagger == null) {
            try {
                Class.forName("io.swagger.annotations.ApiModel");
                swagger = true;
            } catch (ClassNotFoundException e) {
                swagger = false;
            }
        }
        return swagger;
    }

    /**
     * 设置表名前缀
     *
     * @param tablePrefix 设置表名前缀
     * @return 配置选项
     */
    public Option tablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        return this;
    }

    /**
     * 设置生成验证注解
     *
     * @param validateAnnotation 是否验证注解
     * @return 配置选项
     */
    public Option validateAnnotation(boolean validateAnnotation) {
        this.validateAnnotation = validateAnnotation;
        return this;
    }

    /**
     * 设置假删除字段
     *
     * @param deleteMark 是否debuggable
     * @return 配置选项
     */
    public Option deleteMark(String deleteMark) {
        this.deleteMark = deleteMark;
        return this;
    }

    public boolean isDeleteMark() {
        return deleteMark != null && !deleteMark.trim().isEmpty();
    }

    /**
     * 设置假删除值
     *
     * @param deleteValue 是否debuggable
     * @return 配置选项
     */
    public Option deleteValue(String deleteValue) {
        this.deleteValue = deleteValue;
        return this;
    }

    /**
     * 是否生成debuggable
     *
     * @param debuggable 是否debuggable
     * @return 配置选项
     */
    public Option debuggable(boolean debuggable) {
        this.debuggable = debuggable;
        return this;
    }

    /**
     * 是否生成lombok
     *
     * @param lombok 是否lombok
     * @return 配置选项
     */
    public Option lombok(boolean lombok) {
        this.lombok = lombok;
        return this;
    }

    /**
     * xml是否生成在resources目录下,否将生成在classpath下
     *
     * @param resourcesXml 是否生成在resources目录下
     * @return 配置选项
     */
    public Option resourcesXml(boolean resourcesXml) {
        this.resourcesXml = resourcesXml;
        return this;
    }

    /**
     * 配置swagger
     *
     * @param swagger 是否swagger注释
     * @return 配置选项
     */
    public Option swagger(boolean swagger) {
        this.swagger = swagger;
        return this;
    }

    /**
     * 生成方法注释
     *
     * @param methodAnnotation 生成方法注释
     * @return 配置选项
     */
    public Option methodAnnotation(boolean methodAnnotation) {
        this.methodAnnotation = methodAnnotation;
        return this;
    }
}
