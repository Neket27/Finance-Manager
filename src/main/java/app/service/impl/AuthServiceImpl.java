package app.service.impl;

import app.aspect.auditable.Auditable;
import app.context.UserContext;
import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.Token;
import app.exception.ErrorLogoutException;
import app.exception.NotFoundException;
import app.exception.UserAlreadyExistsException;
import app.exception.UserIsAlreadyLoggedInException;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorRegisterExeption;
import app.service.AuthService;
import app.service.TokenService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Реализация сервиса аутентификации.
 */

@Service
public class AuthServiceImpl implements AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserService userService;
    private TokenService tokenService;

    /**
     * Конструктор сервиса аутентификации.
     */
    public AuthServiceImpl(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param userDto данные пользователя для регистрации
     * @return userDto, если регистрация успешна
     */
    @Override
    @Auditable
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
    @Auditable
    public ResponseLogin login(SignIn signin) {
        UserDto user;

        try {
            user = userService.getUserByEmail(signin.email());

        } catch (NotFoundException e) {
            log.debug("User with email {} not found", signin.email());
            throw new ErrorLoginExeption(e.getMessage());
        }


        if (signin.password().equals(user.password())) {
            Integer key = new Random().nextInt(100);

            Token token = new Token.Builder()
                    .userId(user.id())
                    .value(key.toString())
                    .build();
            tokenService.saveToken(token);

            UserContext.setCurrentUser(user);
            log.debug("Authenticated user: " + signin.email());
            return new ResponseLogin(user.id().toString());
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
    @Auditable
    public void logout() {
        UserDto user = UserContext.getCurrentUser();
        if (user == null)
            throw new ErrorLogoutException("You are not logged in");

        tokenService.deleteTokenByUserId(user.id());

        UserContext.clear();
    }
}
