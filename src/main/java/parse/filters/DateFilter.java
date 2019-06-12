package parse.filters;

/**
 * @author wangguowei
 * @description
 * @create 2019-04-25-16:36
 */
public class DateFilter implements IFilter {

    public String filter(String source) {
        if(source==null || source.equals("")){
            return source;
        }
        source = source.replaceAll("<[^<].*?>","");
        source = source.replaceAll(" "," ");
        source = source.replaceAll("&nbsp;"," ");
        source = source.replaceAll("\"","");
        source = source.replaceAll("\\.","-");
        source = source.replace("年","-");
        source = source.replace("月",source.contains("日")?"-":"");
        source = source.replace("日","").trim();
        return source;
    }
}
