package app.service.impl;

import app.auth.Authenticator;
import app.config.AuthenticationConfig;
import app.context.UserContext;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.exeption.NotFoundException;
import app.exeption.UserIsAlreadyLoggedInException;
import app.service.AuthService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Реализация сервиса аутентификации.
 */
public class AuthServiceImpl implements AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationConfig authenticationConfig;
    private final Authenticator authenticator;
    private final UserService userService;

    /**
     * Конструктор сервиса аутентификации.
     *
     * @param authenticationConfig конфигурация аутентификации
     * @param authenticator        обработчик аутентификации
     * @param userService          сервис пользователей
     */
    public AuthServiceImpl(AuthenticationConfig authenticationConfig, Authenticator authenticator, UserService userService) {
        this.authenticationConfig = authenticationConfig;
        this.authenticator = authenticator;
        this.userService = userService;
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param userDto данные пользователя для регистрации
     * @return true, если регистрация успешна, иначе false
     */
    @Override
    public boolean register(CreateUserDto userDto) {
        try {
            userService.createUser(userDto);
            log.debug("Registered user: " + userDto);
            return true;
        } catch (UserIsAlreadyLoggedInException e) {
            log.debug("User with email {} is already logged in", userDto.email());
            return false;
        }
    }

    /**
     * Выполняет вход пользователя.
     *
     * @param email    электронная почта пользователя
     * @param password пароль пользователя
     * @return true, если вход выполнен успешно, иначе false
     */
    @Override
    public boolean login(String email, String password) {
        UserDto user;
        try {
            user = userService.getUserByEmail(email);
        } catch (NotFoundException e) {
            log.debug("User with email {} not found", email);
            return false;
        }

        if (authenticator.checkCredentials(email, password)) {
            UserContext.setCurrentUser(user);
            return true;
        }

        try {
            authenticationConfig.addCredential(user);
            UserContext.setCurrentUser(user);
            log.debug("Authenticated user: " + email);
            return true;
        } catch (Exception e) {
            log.debug("Invalid password or email", email);
            return false;
        }
    }

    /**
     * Выполняет выход пользователя.
     *
     * @return true, если выход выполнен успешно, иначе false
     */
    @Override
    public boolean logout() {
        boolean userCredentialsDeleted = authenticator.clearCredentials(UserContext.getCurrentUser().email());
        UserContext.clear();
        return userCredentialsDeleted;
    }
}
