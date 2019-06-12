package parse.annotation;


import parse.filters.BaseFilter;

import java.lang.annotation.*;

/**
 * 此注解标识某字段解析后，是否需要过滤
 * 例：某css选择器或者xpach继续到的内容，需要进一步处理，则需要带此注解，
 * 反射到指定的过滤器执行进一步解析工作
 * @author wangguowei
 * @description
 * @create 2019-04-11-17:57
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Filter {
    Class<?> value() default BaseFilter.class;
}
