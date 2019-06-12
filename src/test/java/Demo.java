import parse.annotation.Rule;
import parse.annotation.Split;

import java.util.List;

/**
 * @author wangguowei
 * @description
 * @create 2019-06-12-14:39
 */
public class Demo {

    /*如果带规则，则o1以下的所有层，都基于此规则提取的html节点进行解析*/
    //@Rule("div#test")
    private Object1 o1;
    @Split("table:contains(姓名) tr:gt(0)")
    private List<Object2> list;

    public Object1 getO1() {
        return o1;
    }

    public void setO1(Object1 o1) {
        this.o1 = o1;
    }

    public List<Object2> getList() {
        return list;
    }

    public void setList(List<Object2> list) {
        this.list = list;
    }
}
