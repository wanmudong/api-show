package top.wanmudong.apishow.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author wanmudong
 * @date 20:09 2019/3/29
 */

@Data
@AllArgsConstructor
public class ApiCodeDefinition {

    private String code;
    private String description;

}
