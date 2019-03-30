package top.wanmudong.apishow.utils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import javax.servlet.ServletOutputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author wanmudong
 * @date 13:38 2019/3/30
 */
public class GenUtil {

    public static void generateMarkdown(Map<String, Object> data, String template, OutputStream outputStream) {

        //初始化模板引擎
        Velocity.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        //获取模板文件
        Template tem = Velocity.getTemplate(template,"UTF-8");

        //设置变量
        VelocityContext context = new VelocityContext();

        for (Map.Entry<String, Object> entry : data.entrySet()){

            context.put(entry.getKey(),entry.getValue());

        }


        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            tem.merge(context,writer);

            writer.flush();

        } catch (IOException e) {

            e.printStackTrace();

        } ;


    }

    public static String generateMarkdown(Map<String, Object> data, String template) {

        //初始化模板引擎
        Velocity.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();

        //获取模板文件
        Template tem = Velocity.getTemplate(template,"UTF-8");

        //设置变量
        VelocityContext context = new VelocityContext();

        for (Map.Entry<String, Object> entry : data.entrySet()){

            context.put(entry.getKey(),entry.getValue());

        }

        StringWriter writer = new StringWriter();

        tem.merge(context, writer);

        return writer.toString();

    }
}
