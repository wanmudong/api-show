package top.wanmudong.apishow.annotation;

import top.wanmudong.apishow.utils.DataType;

import java.lang.annotation.*;

/**
 * @author wanmudong
 * @date 19:12 2019/3/29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ApiParam {

    /**
     * 参数名
     */
    String name();

    /**
     * 参数说明
     */
    String description() default "";

    /**
     * 参数默认值
     */
    String defaultValue() default "";

    /**
     * 是否必选
     */
    boolean required() default false;

    /**
     * 示例
     */
    String example() default "";

    /**
     * 数据类型
     */
    DataType dataType() default DataType.STRING;
}
