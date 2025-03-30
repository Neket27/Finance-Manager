package app.service.impl;

import app.aspect.auditable.Auditable;
import app.aspect.loggable.CustomLogging;
import app.context.UserContext;
import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.Token;
import app.exception.user.UserException;
import app.exception.auth.ErrorLogoutException;
import app.exception.user.UserAlreadyExistsException;
import app.exception.user.UserIsAlreadyLoggedInException;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorRegistrationException;
import app.service.AuthService;
import app.service.TokenService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@CustomLogging
public class AuthServiceImpl implements AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserService userService;
    private TokenService tokenService;


    public AuthServiceImpl(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    @Auditable
    public UserDto register(CreateUserDto userDto) {
        try {
            UserDto user = userService.createUser(userDto);
            log.debug("Registered user: " + userDto);
            return user;
        } catch (UserAlreadyExistsException | UserIsAlreadyLoggedInException e) {
            log.debug("User with email {} is already logged in", userDto.email());
            throw new ErrorRegistrationException("User with email " + userDto.email() + " is already logged in");
        }
    }

    @Override
    @Auditable
    public ResponseLogin login(SignIn signin) {
        UserDto user;

        try {
            user = userService.getUserByEmail(signin.email());

        } catch (UserException e) {
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
