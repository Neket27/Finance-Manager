package neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CustomLogging {
}
