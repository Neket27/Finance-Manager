package app.config;

import app.entity.User;
import app.exeption.UserIsAlreadyLoggedInExeption;

import java.util.Map;

public class AuthenticationConfig {

    private final Map<String, User> credentials;

    public AuthenticationConfig(Map<String, User> credentials) {
        this.credentials = credentials;
    }

    public void addCredential(User user) {
        if (!credentials.containsKey(user.getEmail()))
            credentials.put(user.getEmail(), user);
        else
            throw new UserIsAlreadyLoggedInExeption("Пользователь уже авторизирован");
    }

    public Map<String, User> getCredentials() {
        return credentials;
    }
}
