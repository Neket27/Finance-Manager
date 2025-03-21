package test.config;

import app.dto.user.UserDto;
import app.exception.UserIsAlreadyLoggedInException;

import java.util.Map;

public class AuthenticationConfig {

    private final Map<String, UserDto> credentials;

    public AuthenticationConfig(Map<String, UserDto> credentials) {
        this.credentials = credentials;
    }

    public void addCredential(UserDto user) {
        if (!credentials.containsKey(user.email()))
            credentials.put(user.email(), user);
        else
            throw new UserIsAlreadyLoggedInException("Пользователь уже авторизирован");
    }

    public Map<String, UserDto> getCredentials() {
        return credentials;
    }
}
