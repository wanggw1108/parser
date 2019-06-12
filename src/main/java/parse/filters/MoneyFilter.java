package parse.filters;


/**
 * @author wangguowei
 * @description
 * @create 2019-04-19-17:58
 */
public class MoneyFilter implements IFilter{

    public String filter(String source) {
        if(source==null || source.equals("")){
            return source;
        }
        source = source.replaceAll("<[^<].*?>","");
        source = source.replaceAll(",","").trim();
        return source;
    }
}
