package parse.annotation;

import java.lang.annotation.*;

/**
 *
 * 解析规则注解，
 * 以指定的规则进行解析
 * @author wangguowei
 * @description
 * @create 2019-04-11-16:57
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Rule {

    /**
     * 解析规则
     * @return
     */
    String value();
    /**
     * 规则类型，xpath，css，regex,jpath四种类型
     * @return
     */
    RuleType type() default RuleType.CSS;

    /**
     * 此规则是否作用在整个页面
     * 默认只作用在带Rule注解的实体解析到的源码
     * @return
     */
    boolean all() default false;


}
