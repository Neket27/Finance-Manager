package app;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Component
@ComponentScan
@EnableWebMvc
@EnableAspectJAutoProxy
public class App {

    public static void main(String... args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(App.class);
    }

}
