package neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable;

import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.config.CustomLoggerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomLoggerConfiguration.class)
public @interface EnableCustomLogger {
}


