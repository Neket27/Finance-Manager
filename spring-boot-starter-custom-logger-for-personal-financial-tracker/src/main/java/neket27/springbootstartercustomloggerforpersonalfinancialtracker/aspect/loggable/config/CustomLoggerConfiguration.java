package neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.config;

import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.LoggableAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
public class CustomLoggerConfiguration {

    @Bean
    public LoggableAspect loggableAspect() {
        return new LoggableAspect();
    }
}
