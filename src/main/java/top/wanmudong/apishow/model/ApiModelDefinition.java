package top.wanmudong.apishow.model;

import lombok.Data;

import java.util.List;

/**
 * @author wanmudong
 * @date 20:09 2019/3/29
 */
@Data
public class ApiModelDefinition {

    private String name;
    private String description;
    private String remark;

    private List<ApiModelPropertyDefinition> propertyList;

}
