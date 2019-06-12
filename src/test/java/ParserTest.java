import com.alibaba.fastjson.JSONObject;
import parse.Parser;

import java.lang.reflect.InvocationTargetException;

/**
 * @author wangguowei
 * @description
 * @create 2019-06-12-14:33
 */
public class ParserTest {


    public static void main(String[] args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        String testHtml = testHtml();
        Demo demo = Parser.init(testHtml).exec(Demo.class);
        System.out.println(JSONObject.toJSONString(demo));

    }

    public static String testHtml(){
        String html = "<html>\n" +
                "<header>\n" +
                "\n" +
                "</header>\n" +
                "<body>\n" +
                "<div id='test'>\n" +
                "<div>2</div>\n" +
                "<div id='123'>测试jsoup选择器</div>\n" +
                "<div>金额：12.5元</div>\n" +
                "</div>\n" +
                "<table>\n" +
                "<tr>\n" +
                "<td>姓名</td>\n" +
                "<td>年龄</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>小花</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>小明</td>\n" +
                "<td>8</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>小王</td>\n" +
                "<td>5</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "<table>\n" +
                "<tr>\n" +
                "<td>name</td>\n" +
                "<td>age</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>小花</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>小明</td>\n" +
                "<td>8</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>小王</td>\n" +
                "<td>5</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";
        return html;
    }
}
