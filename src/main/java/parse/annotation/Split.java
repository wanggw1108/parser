package parse.annotation;

import java.lang.annotation.*;

/**
 * @author wangguowei
 * @description
 * @create 2019-04-11-19:26
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Split {
    String value();
}
