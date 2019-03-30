package top.wanmudong.apishow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;


/**
 * @author wanmudong
 * @date 19:45 2019/3/29
 */
@Data
@ConfigurationProperties(prefix = "api-show")
public class ApiShowProperties {

    public  static final String PREFIX="api-show";

    /**
     * 是否开启api-show
     */
    private boolean enable = true;

    /**
     * 控制器所在的包名
     */
    private String controllerPackage;

    /**
     * 数据模型所在的包名
     */
    private String modelPackage;

    /**
     * api的版本号
     */
    private String version;

    /**
     * api所在的主机地址
     */
    private String host;

    /**
     * 联系方式
     */
    @NestedConfigurationProperty
    private Contact contact;

    @Data
    public static class Contact{
        /**
         * 联系人姓名
         */
        private String name;

        /**
         * 联系人邮箱
         */
        private  String email;
    }
}
