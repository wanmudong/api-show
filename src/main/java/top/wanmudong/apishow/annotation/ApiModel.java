package top.wanmudong.apishow.annotation;

import java.lang.annotation.*;

/**
 * @author wanmudong
 * @date 19:12 2019/3/29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ApiModel {
    /**
     * 数据模型说明
     */
    String description();
    /**
     * 备注
     */
    String remark();
}
