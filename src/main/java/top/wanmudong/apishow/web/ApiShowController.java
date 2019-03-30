package top.wanmudong.apishow.web;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.wanmudong.apishow.core.ApiShowContext;
import top.wanmudong.apishow.model.ApiModelDefinition;
import top.wanmudong.apishow.model.Document;
import top.wanmudong.apishow.utils.GenUtil;
import top.wanmudong.apishow.utils.MarkdownEntity;
import top.wanmudong.apishow.utils.MarkdownUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wanmudong
 * @date 13:19 2019/3/30
 */
@Controller
@RequestMapping(value = "/api")
public class ApiShowController {

    @ResponseBody
    @RequestMapping(value = "/show")
    public Map<String, Object> show(){
        HashMap<String, Document> docMap = ApiShowContext.getDocMap();
        HashMap<String, ApiModelDefinition> modelMap = ApiShowContext.getModelMap();

        Map<String,Object> data = new HashMap<>(4);

        data.put("apiList",docMap);
        data.put("modelList",modelMap);
        return data;
    }

    @GetMapping(value = "/markdown")
    public void showMarkDown(HttpServletResponse response) throws IOException{
        HashMap<String, Document> docMap = ApiShowContext.getDocMap();
        HashMap<String, ApiModelDefinition> modelMap = ApiShowContext.getModelMap();

        Map<String, Object> data = new HashMap<>(4);

        data.put("apiList", docMap.values());
        data.put("modelList", modelMap.values());

        // 设置响应头，控制浏览器下载该文件
        response.setHeader("content-disposition", "attachment;filename=api-show.md");

        GenUtil.generateMarkdown(data, "md/doc.md.vm", response.getOutputStream());
    }


    @GetMapping(value = "/html")
    public void showHtml(HttpServletResponse response) throws IOException{

        HashMap<String, Document> docMap = ApiShowContext.getDocMap();
        HashMap<String, ApiModelDefinition> modelMap = ApiShowContext.getModelMap();

        Map<String, Object> data = new HashMap<>(4);

        data.put("apiList", docMap.values());
        data.put("modelList", modelMap.values());

        response.setHeader("Content-Type", "text/html");
        response.setHeader("Keep-Alive", "timeout=30, max=100");

        String markdown = GenUtil.generateMarkdown(data, "md/doc.md.vm");

        MarkdownEntity entity = MarkdownUtil.ofContent(markdown);

        FileCopyUtils.copy(entity.getHtml().getBytes(), response.getOutputStream());

    }


}
