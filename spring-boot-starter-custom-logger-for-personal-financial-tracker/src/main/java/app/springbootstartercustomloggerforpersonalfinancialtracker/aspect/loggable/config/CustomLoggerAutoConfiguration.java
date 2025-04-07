package app.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.config;

import app.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.LoggableAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
public class CustomLoggerAutoConfiguration {

    @Bean
    public LoggableAspect loggableAspect() {
        return new LoggableAspect();
    }
}
