package parse.filters;

/**
 * @author wangguowei
 * @description 基本过滤器，过滤标签，占位符替换。
 * @create 2019-04-11-22:45
 */
public class BaseFilter implements IFilter{

    public String filter(String source){
        if(source==null || source.equals("")){
            return source;
        }
        source = source.replaceAll("<[^<].*?>","");
        source = source.replaceAll(" "," ");
        source = source.replaceAll("&nbsp;"," ");
        source = source.replaceAll("\"","");
        source = source.replaceAll("\\.","-").trim();
        return source;
    }
    public static void  main(String[] args){
        String a = new BaseFilter().filter("11111122221");
        System.out.println(a);
    }
}
