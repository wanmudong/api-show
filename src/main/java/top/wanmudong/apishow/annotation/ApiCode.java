package top.wanmudong.apishow.annotation;

import java.lang.annotation.*;

/**
 * @author wanmudong
 * @date 19:12 2019/3/29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface ApiCode {
    /**
     * 错误码
     */
    String code();

    /**
     * 错误说明
     */
    String description();
}
