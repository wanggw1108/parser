import parse.annotation.Filter;
import parse.annotation.Rule;
import parse.annotation.RuleType;
import parse.annotation.Split;
import parse.filters.MoneyFilter;

import java.util.List;

/**
 * @author wangguowei
 * @description
 * @create 2019-06-12-14:39
 */
public class Object1 {
    /*jsoup的select选择器语法提取所需字段*/
    @Rule("div#123")
    /*Fileter为过滤器，定义此字段值的过滤方式，默认仅剔除标签，保留文本*/
    @Filter
    private String title;
    /*此注解指定解析语法为正则解析，值为group(1)*/
    @Rule(value="金额：([^<]*?)元" ,type= RuleType.REGEX)
    /*此注解指定过滤器，过滤器可以根据实际需要去实现，并应用到需特殊处理的字段*/
    @Filter(MoneyFilter.class)
    private String price;
    /*此规则为select选择器+扩展语法，
    * next：指定元素下开始，同级第2个元素
    * child：指定元素的第n个子元素
    * parent：指定元素的第n个父元素
    *
    * */
    @Rule("div#123|{next|1}")
    @Filter(MoneyFilter.class)
    private String testNext;
    @Rule("table:contains(姓名) tr:eq(0)")
    @Split("td")
    private List<String> stringList;

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public String getTestNext() {
        return testNext;
    }

    public void setTestNext(String testNext) {
        this.testNext = testNext;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
