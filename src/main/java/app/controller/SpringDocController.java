package app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpringDocController {

    @GetMapping("/ui")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui";
    }
}
