package app;

import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.EnableCustomLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableCustomLogger
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
