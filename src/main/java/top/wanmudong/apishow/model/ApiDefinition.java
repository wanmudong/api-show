package top.wanmudong.apishow.model;

import lombok.Data;

import java.util.List;

/**
 * @author wanmudong
 * @date 20:09 2019/3/29
 */
@Data
public class ApiDefinition {

    private String name;
    private String path;
    private String description;
    private String remark;
    private String httpMethod;

    private String demoResponse;

    private List<ApiParamDefinition> requestParams;
    private List<ApiParamDefinition> responseParams;

    private ApiModelDefinition resultModel;

}
