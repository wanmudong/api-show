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
public @interface ApiModelProperty {

    /**
     * 属性名
     */
    String name();

    /**
     * 属性说明
     */
    String description();

    /**
     * 默认值
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
