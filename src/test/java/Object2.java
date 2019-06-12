import parse.annotation.Filter;
import parse.annotation.Rule;

/**
 * @author wangguowei
 * @description
 * @create 2019-06-12-14:39
 */
public class Object2 {
    @Rule("td:eq(0)")
    @Filter
    private String name;
    @Rule("td:eq(1)")
    @Filter
    private String age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
