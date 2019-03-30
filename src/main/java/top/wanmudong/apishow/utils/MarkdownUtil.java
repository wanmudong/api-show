package top.wanmudong.apishow.utils;

import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.options.MutableDataSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;

/**
 * @author wanmudong
 * @date 13:55 2019/3/30
 */
public class MarkdownUtil {

    private static final String MD_CSS_HREF = "css/good.css";

    private static String cssCache;


    /**
     * 将本地的markdown文件，转为html文档输出
     *
     * @param path 相对地址or绝对地址 ("/" 开头)
     */
    public static MarkdownEntity ofFile(String path) throws IOException {
        return ofStream(new FileInputStream(path));
    }


    /**
     * 将流转为html文档输出
     *
     * @param stream
     * @return
     */
    public static MarkdownEntity ofStream(InputStream stream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(stream, Charset.forName("UTF-8")));

        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = bufferedReader.readLine()) != null) {

            sb.append(line);

        }

        bufferedReader.close();

        stream.close();

        return ofContent(sb.toString());
    }


    /**
     * 直接将markdown语义的文本转为html格式输出
     *
     * @param content markdown语义文本
     */
    public static MarkdownEntity ofContent(String content) {

        String html = parse(content);

        MarkdownEntity entity = new MarkdownEntity();

        if (StringUtils.isEmpty(cssCache)){

            cssCache=loadCss("css/good.css");

        }

        entity.setHtmlBody(html);
        entity.setCss(cssCache);

        return entity;
    }

    private static String loadCss(String cssPath) {

        ClassPathResource cpr = new ClassPathResource(cssPath);

        StringBuilder css = new StringBuilder();

        try (InputStream is = cpr.getInputStream();

             InputStreamReader isr = new InputStreamReader(is);

             BufferedReader reader = new BufferedReader(isr)) {

            String line;

            while ((line = reader.readLine()) != null) {

                css.append(line);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return css.toString();
    }

    /**
     * markdown to image
     *
     * @param content markdown contents
     * @return parse html contents
     */
    public static String parse(String content) {
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);

        // enable table parse!
        options.set(Parser.EXTENSIONS, Collections.singletonList(TablesExtension.create()));


        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(content);
        return renderer.render(document);
    }

}
