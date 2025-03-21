package app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;

public class ConfigLoader {
    public static <T> T loadConfig(String fileName, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new RuntimeException("Файл " + fileName + " не найден в resources");
            }
            return mapper.readValue(in, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки YAML", e);
        }
    }
}
