package app.config;

import app.container.Bean;
import app.container.Configuration;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JsonMapperConfig {

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }
}
