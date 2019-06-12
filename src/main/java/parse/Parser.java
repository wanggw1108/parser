package parse;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import parse.annotation.Filter;
import parse.annotation.Rule;
import parse.annotation.Rules;
import parse.annotation.Split;
import parse.filters.BaseFilter;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析核心
 * 支持任意规则的自定义对象解析
 * @author wangguowei
 * @description
 * @create 2019-04-11-17:17
 */
public class Parser{
    private  static HashMap<String,Pair<Object,Method>> filters = new HashMap<>();
    private static HashMap<String,Method> method_list = new HashMap<>();
    private static Pattern select_index = Pattern.compile("(.*?)\\|\\{(\\d+)\\}");
    private static Pattern select_parent = Pattern.compile("(.*?)\\|\\{parent\\|(\\d+)\\}");
    private static Pattern select_child = Pattern.compile("(.*?)\\|\\{child\\|(\\d+)\\}");
    private static Pattern select_next = Pattern.compile("(.*?)\\|\\{next\\|(\\d+)\\}");
    private static Pattern select_step = Pattern.compile("(.*?)\\|\\{step\\|(\\d+)\\}");
    private static Pattern regex_or = Pattern.compile("(.*?)\\|\\|(.*?)");
    String html;
    Document document;
    private Parser(String html){
        this.html = html;
        this.document = Jsoup.parse(formatHtml(html));
    }
    private Parser(Document document){
        this.html = document.html();
        this.document = document;
    }
    public static Parser init(String html){
        return new Parser(html);
    }
    public static Parser init(Document document){
        return new Parser(document);
    }
    public <T> T exec(Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Field[] type_fields = clazz.getDeclaredFields();
        Pair<Field[],Field[]> pair = fieldFilter(type_fields);
        Field[] base_field = pair.getValue();
        type_fields = pair.getKey();
        Object result = clazz.newInstance();
        //基本数据类型，先解析
        for(Field base:base_field){
            String value = parse_field_source(base,html);
            //如果是字符串集合
            if(base.getGenericType()!=null && base.getGenericType() instanceof ParameterizedType){
                Split split = base.getAnnotation(Split.class);
                if(split!=null){
                    List<String> strList = split(split,value,true);
                    if(strList!=null){
                        setValue(result,clazz,base.getName(),strList.size()==0?null:strList);
                    }
                }
                continue;
            }
            setValue(result,clazz,base.getName(),value);
        }
        //对象类型，递归解析
        for(Field f:type_fields){
            Class f_clazz = f.getType();
            String value = parse_field_source(f,html);
            if(StringUtil.isBlank(value)){
                setValue(result,clazz,f.getName(),null);
                continue;
            }
            //区分一下集合对象和单个对象。
            if(f.getType() == List.class){
                Class obj_clazz = f.getType();
                Type genericType = f.getGenericType();
                if(genericType == null) continue;
                // 如果是泛型参数的类型
                if(genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericType;
                    //得到泛型里的class类型对象
                    obj_clazz = (Class<?>) pt.getActualTypeArguments()[0];
                }
                //给集合属性赋值
                setValue(result,clazz,f.getName(),parseListField(value,obj_clazz,f));
            }else {
                //给对象属性赋值
                setValue(result,clazz,f.getName(),Parser.init(value).exec(f_clazz));
            }
        }
        return JSON.toJSONString(result).equals("{}")?null:(T) result;
    }

    private List<String> split(Split split,String source,boolean filter) {
        Matcher m = select_step.matcher(split.value());
        int step = 0;
        String rule = split.value();
        if (m.find()) {
            step = Integer.valueOf(m.group(2));
            rule = m.group(1);
        }
        Elements es = Jsoup.parse(formatHtml(source)).select(rule);
        if (es != null) {
            List<String> cells = new ArrayList();
            if(step==0){
                for (int i = 1; i <= es.size(); i++) {
                    String field_source = es.get(i - 1).outerHtml();
                    cells.add(filter?new BaseFilter().filter(field_source):field_source);
                }
                return cells;
            }else {
                for(int i=1;i<es.size();i++){
                    if(i%step==0){
                        String field_source = es.get(i-1).outerHtml();
                        cells.add(filter?new BaseFilter().filter(field_source):field_source);
                    }
                }
                return cells;
            }
        }
        return null;
    }

    /**
     * 属性分组
     * 对象一组，基本数据一组
     * @param fields
     * @return
     */
    private Pair<Field[],Field[]> fieldFilter(Field[] fields){
        List<Field> obj_f = new ArrayList<>();
        List<Field> f_f = new ArrayList<>();
        for(Field f:fields){
            Class clazz = f.getType();
            Type genericType = f.getGenericType();
            if(genericType == null) continue;
            // 如果是泛型参数的类型
            if(genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                //得到泛型里的class类型对象
                clazz = (Class<?>) pt.getActualTypeArguments()[0];
            }
            //如果不是基本数据类型
            if(!isBaseDataType(clazz)){
                obj_f.add(f);
            }else {
                f_f.add(f);
            }
        }
        Pair<Field[],Field[]> pair = new Pair<>(obj_f.toArray(new Field[0]),f_f.toArray(new Field[0]));
        return pair;
    }

    /**
     * 判断一个类是否为基本数据类型。
     * @param clazz 要判断的类。
     * @return true 表示为基本数据类型。
     */
    private static boolean isBaseDataType(Class clazz) {
        return
                (clazz.equals(String.class) ||
                        clazz.equals(Integer.class)||
                        clazz.equals(Byte.class) ||
                        clazz.equals(Long.class) ||
                        clazz.equals(Double.class) ||
                        clazz.equals(Float.class) ||
                        clazz.equals(Character.class) ||
                        clazz.equals(Short.class) ||
                        clazz.equals(BigDecimal.class) ||
                        clazz.equals(BigInteger.class) ||
                        clazz.equals(Boolean.class) ||
                        clazz.equals(Date.class) ||
                        clazz.isPrimitive()
                );
    }
    private  Object parseListField(String field_source, Class<?> clazz, Field c_f) throws IllegalAccessException, InstantiationException {
        field_source = formatHtml(field_source);
        field_source =  parse_field_source(c_f,field_source);
        Object cell_obj = null;
            Split split = c_f.getAnnotation(Split.class);
            if(split==null){
                return null;
            }
            List<String> field_source_s = split(split,field_source,false);
            List cells = new ArrayList();
            for(String source:field_source_s){
                if(!StringUtil.isBlank(source)){
                    Object o = null;
                    try {
                        o = Parser.init(source).exec(clazz);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    if(o!=null){
                        cells.add(o);
                    }
                }
            }
        cell_obj = cells.size()==0?null:cells;
        return cell_obj;
    }
    private String filter(Filter filter, String source){
        if(filter!=null){
            try {
                String clazzName = filter.value().getName();
                if(!filters.containsKey(clazzName)){
                    Object o = filter.value().newInstance();
                    Method m = filter.value().getMethod("filter",String.class);
                    filters.put(clazzName,new Pair<Object,Method>(o,m));
                }
                Pair<Object,Method> pair = filters.get(clazzName);
                source = (String) pair.getValue().invoke(pair.getKey(),source);
            } catch (Exception e1) {
                e1.printStackTrace();
                return null;
            }
        }
        return source;
    }

    /**
     *
     * @param o 要构造的对象
     * @param clazz 所属类
     * @param name 要构造的属性
     * @param value 属性值
     */
    private  void setValue(Object o,Class<?> clazz,String name,Object value){
        if(!method_list.containsKey(clazz.getName()+name)){
            Method method = getSetMethod(clazz, name);
            method_list.put(clazz.getName()+name,method);
        }
        try {
            method_list.get(clazz.getName()+name).invoke(o,value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    private  Method getSetMethod(Class objectClass, String fieldName) {
        try {
            Class[] parameterTypes = new Class[1];
            Field field = objectClass.getDeclaredField(fieldName);
            parameterTypes[0] = field.getType();
            StringBuffer sb = new StringBuffer();
            sb.append("set");
            sb.append(fieldName.substring(0, 1).toUpperCase());
            sb.append(fieldName.substring(1));
            Method method = objectClass.getMethod(sb.toString(), parameterTypes);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 定位指定规则的源码 for cell
     * @return
     */
    /**
     * 定位指定规则的源码 for cell->flelds
     * @return
     */
    private  String  parse_field_source(Field field,String source){
        try{
            Rule r = field.getAnnotation(Rule.class);
            Rules rules = field.getAnnotation(Rules.class);
            Filter filter = field.getAnnotation(Filter.class);
            ArrayList<Rule> rule_list = r!=null? Lists.newArrayList(new Rule[]{r}):Lists.newArrayList();
            rule_list = rules!=null? Lists.newArrayList(rules.value()):rule_list;
            if(isBaseDataType(field.getType())&&rule_list.size()==0&&filter==null){
                return null;
            }
            //解析出此单元的数据源码
            if(rule_list.size()>=1){
                for(int i=0;i<rule_list.size();i++){
                    //第一个规则，使用document,避免实例化相同的document，提高解析效率
                    source = parse(source,rule_list.get(i),i);
                }
            }

            //如果有过滤器，则再经过一次过滤过程
            source = filter(filter,source);
            return source;
        }catch (Exception e){
            return null;
        }
    }

    public  String parse(String source,Rule rule,int index){
        if(StringUtil.isBlank(source)){
            return null;
        }
        switch (rule.type()){
            case CSS:{
                Document doc = index==0?document:Jsoup.parse(formatHtml(source));
                Matcher m = select_index.matcher(rule.value());
                Matcher m2 = select_parent.matcher(rule.value());
                Matcher m3 = select_child.matcher(rule.value());
                Matcher m4 = select_next.matcher(rule.value());
                String result = null;
                if(m.find()){
                    Elements es = doc.select(m.group(1));
                    Element e = es.get(Integer.valueOf(m.group(2)));
                    result = e.outerHtml();
                    return result;
                }
                if(m2.find()) {
                    Elements es = doc.select(m2.group(1));
                    Element e = null;
                    e = es.parents().get(Integer.valueOf(m2.group(2))-1);
                    result = e.outerHtml();
                    return result;

                }
                if(m3.find()){
                    Elements es = doc.select(m3.group(1));
                    for(int i=1;i<=Integer.valueOf(m3.group(2));i++){
                        es = es.get(0).children();
                    }
                    result = es.outerHtml();
                    return result;
                }
                if(m4.find()){
                    Elements es = doc.select(m4.group(1));
                    Element e = es.get(0).nextElementSibling();
                    for(int i=2;i<=Integer.valueOf(m4.group(2));i++){
                        e = e.nextElementSibling();
                    }
                    result = e.outerHtml();
                    return result;
                }
                result = doc.select(rule.value()).outerHtml();
                return result;
            }
            case REGEX:{
                String[] regexs = rule.value().split("\\|\\|");
                for(String regex:regexs){
                    Pattern pattern = Pattern.compile(regex);
                    Matcher m = pattern.matcher(index==0?html:source);
                    if(m.find()){
                        return m.group(1);
                    }
                }
                return  null;
            }
            case XPATH:{
//                return WebMagicUtil.getStr(rule.value(),new Html(source));
                return null;
            }
            case JPATH:{
//                ReadContext ctx = JsonPath.parse(source);
//                return ctx.read(rule.value(),String.class);
                return null;
            }
        }
        return null;
    }

    public String formatHtml(String source){
        if(!StringUtil.isBlank(source)&&source.contains("<td")&&!source.contains("<tr")){
            StringBuilder builder = new StringBuilder("<tr>");
            builder.append(source).append("</tr>");
            source = builder.toString();
        }
        if(!StringUtil.isBlank(source)&&source.contains("<th")&&!source.contains("<tr")){
            StringBuilder builder = new StringBuilder("<tr>");
            builder.append(source).append("</tr>");
            source = builder.toString();
        }
        if(!StringUtil.isBlank(source)&&source.contains("<tr")&&!source.contains("<table")){
            StringBuilder builder = new StringBuilder("<table>");
            builder.append(source).append("</table>");
            source = builder.toString();
        }
        return source;
    }

    public  static void main(String[] args){


    }
}
