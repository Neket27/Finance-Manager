package neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action() default "";
}

