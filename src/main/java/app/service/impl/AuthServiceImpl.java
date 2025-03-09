package app.service.impl;

import app.auth.Authenticator;
import app.config.AuthenticationConfig;
import app.context.UserContext;
import app.dto.user.CreateUserDto;
import app.entity.User;
import app.exeption.UserIsAlreadyLoggedInExeption;
import app.exeption.UserNotFoundException;
import app.mapper.UserMapper;
import app.service.AuthService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthServiceImpl implements AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationConfig authenticationConfig;
    private final Authenticator authenticator;
    private final UserMapper userMapper;
    private final UserService userService;

    public AuthServiceImpl(AuthenticationConfig authenticationConfig, Authenticator authenticator, UserMapper userMapper, UserService userService) {
        this.authenticationConfig = authenticationConfig;
        this.authenticator = authenticator;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @Override
    public boolean register(CreateUserDto userDto) {
        try {
            userService.createUser(userDto);
            log.debug("Registered user: " + userDto);
            return true;
        } catch (UserIsAlreadyLoggedInExeption e) {
            log.debug("User with email {} is already logged in", userDto.email());
            return false;
        }
    }

    @Override
    public boolean login(String email, String password) {
        User user;
        try {
            user = userService.getUserByEmail(email);
        }catch (UserNotFoundException e){
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

}
