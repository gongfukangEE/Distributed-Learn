package annotation;

import java.lang.annotation.*;

/**
 * @auther G.Fukang
 * @date 3/27 16:14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpringControllerLimit {

    int errorCode() default 500;

    String errorMsg() default "request limited";
}
