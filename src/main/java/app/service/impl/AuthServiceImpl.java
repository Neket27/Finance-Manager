package app.service.impl;

import app.auth.Authenticator;
import app.config.AuthenticationConfig;
import app.context.UserContext;
import app.dto.auth.Signin;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.exception.NotFoundException;
import app.exception.UserAlreadyExistsException;
import app.exception.UserIsAlreadyLoggedInException;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorRegisterExeption;
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
     * @return userDto, если регистрация успешна
     */
    @Override
    public UserDto register(CreateUserDto userDto) {
        try {
            UserDto user = userService.createUser(userDto);
            log.debug("Registered user: " + userDto);
            return user;
        } catch (UserAlreadyExistsException | UserIsAlreadyLoggedInException e) {
            log.debug("User with email {} is already logged in", userDto.email());
            throw new ErrorRegisterExeption("User with email " + userDto.email() + " is already logged in");
        }
    }

    /**
     * Выполняет вход пользователя.
     */
    @Override
    public UserDto login(Signin signin) {
        UserDto user;
        try {
            user = userService.getUserByEmail(signin.email());
        } catch (NotFoundException e) {
            log.debug("User with email {} not found", signin.email());
            throw new ErrorLoginExeption(e.getMessage());
        }

        if (authenticator.checkCredentials(signin.email(), signin.password())) {
            UserContext.setCurrentUser(user);
            return user;
        }

        if (signin.password().equals(user.password())) {
            authenticationConfig.addCredential(user);
            UserContext.setCurrentUser(user);
            log.debug("Authenticated user: " + signin.email());
            return user;
        }

        log.debug("Invalid password or email", signin.email());
        throw new ErrorLoginExeption("Invalid password or email");
    }

    /**
     * Выполняет выход пользователя.
     *
     * @return true, если выход выполнен успешно, иначе false
     */
    @Override
    public boolean logout() {
        UserDto user = UserContext.getCurrentUser();
        if (user == null)
            return false;

        boolean userCredentialsDeleted = authenticator.clearCredentials(user.email());
        UserContext.clear();
        return userCredentialsDeleted;
    }
}
