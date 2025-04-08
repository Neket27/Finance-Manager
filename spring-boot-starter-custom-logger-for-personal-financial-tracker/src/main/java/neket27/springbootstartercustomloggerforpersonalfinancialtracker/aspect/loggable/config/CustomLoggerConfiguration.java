package neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.config;

import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.LoggableAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class CustomLoggerConfiguration {

    @Bean
    public LoggableAspect loggableAspect() {
        return new LoggableAspect();
    }
}
