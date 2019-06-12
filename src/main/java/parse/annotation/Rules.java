package parse.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author wangguowei
 * @description
 * @create 2019-04-11-19:59
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Rules {

    Rule[] value();
}
