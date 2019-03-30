package top.wanmudong.apishow.model;

import lombok.Data;
import top.wanmudong.apishow.config.ApiShowProperties;

import java.util.List;

/**
 * @author wanmudong
 * @date 20:09 2019/3/29
 */
@Data
public class Document {

    private String host;
    private ApiShowProperties.Contact contact;
    private String name;
    private String description;
    private String basePath;
    private String version;

    private List<ApiDefinition> apiList;
    private List<ApiCodeDefinition> codeList;

}
