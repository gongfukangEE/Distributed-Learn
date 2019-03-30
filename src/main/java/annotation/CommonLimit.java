package annotation;

import java.lang.annotation.*;

/**
 * @auther G.Fukang
 * @date 3/27 16:11
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommonLimit {
}
