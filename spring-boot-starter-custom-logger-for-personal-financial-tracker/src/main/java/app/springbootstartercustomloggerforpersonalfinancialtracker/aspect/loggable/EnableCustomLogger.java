package app.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable;

import app.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.config.CustomLoggerAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CustomLoggerAutoConfiguration.class)
public @interface EnableCustomLogger {
}
