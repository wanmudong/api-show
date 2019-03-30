package top.wanmudong.apishow.model;

import lombok.Data;

/**
 * @author wanmudong
 * @date 20:09 2019/3/29
 */
@Data
public class ApiParamDefinition {

    private String name;
    private String type;
    private String description;
    private String example;
    private String required;
    private String defaultValue;

    private ApiModelDefinition model;

}
