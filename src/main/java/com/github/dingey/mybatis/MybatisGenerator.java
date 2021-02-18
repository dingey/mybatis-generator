package com.github.dingey.mybatis;

/**
 * mybatis-generator代码生成类,支持链式调用
 */
@SuppressWarnings("unused")
public interface MybatisGenerator {
    /**
     * 设置需要生成的表
     *
     * @param tables 表
     * @return 生成类
     */
    MybatisGenerator table(String... tables);

    /**
     * 设置需要继承的model
     *
     * @param extendsModel 实体抽象类
     * @param <T> 泛型类型
     * @return 生成类
     */
    <T> MybatisGenerator extendsModel(Class<T> extendsModel);

    /**
     * 设置需要继承的model
     *
     * @param mapperInterface 抽象mapper接口
     * @param <T> 泛型类型
     * @return 生成类
     */
    <T> MybatisGenerator extendsMapper(Class<T> mapperInterface);

    /**
     * 生成模型
     *
     * @param packagePath 包路径
     * @return 生成类
     */
    MybatisGenerator generateModel(String packagePath);

    /**
     * 生成mapper接口
     *
     * @param packagePath 包路径
     * @return 生成类
     */
    MybatisGenerator generateMapper(String packagePath);

    /**
     * 生成mapper对应的XML
     *
     * @param mapperXmlPath xml路径
     * @return 生成类
     */
    MybatisGenerator generateXml(String mapperXmlPath);

    /**
     * 设置类型转换
     *
     * @param type      数据库类型
     * @param javaClass java类型
     * @return 生成类
     */
    MybatisGenerator typeAdaptor(String type, Class<?> javaClass);

    /**
     * 设置生成选项
     *
     * @param option 输出调试信息
     * @return 生成类
     */
    MybatisGenerator opt(Option option);
}
