package top.wanmudong.apishow.annotation;

import java.lang.annotation.*;

/**
 * @author wanmudong
 * @date 19:12 2019/3/29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ApiDoc {

    /**
     *文档名称
     */
    String name();

    /**
     * api描述
     */
    String description() default "";

    /**
     *api基本path
     */
    String basePath() default "";

    /**
     *文档版本
     */
    String version() default "";

    /**
     * 响应码
     */
    ApiCode[] codes() default {};
}
