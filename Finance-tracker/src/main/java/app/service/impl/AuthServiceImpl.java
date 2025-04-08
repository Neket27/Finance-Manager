package app.service.impl;


import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.entity.Token;
import app.entity.User;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorLogoutException;
import app.exception.auth.ErrorRegistrationException;
import app.exception.user.UserAlreadyExistsException;
import app.exception.user.UserException;
import app.exception.user.UserIsAlreadyLoggedInException;
import app.service.AuthService;
import app.service.TokenService;
import app.service.UserService;
import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.auditable.Auditable;
import neket27.springbootstartercustomloggerforpersonalfinancialtracker.aspect.loggable.CustomLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neket27.context.UserContext;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@CustomLogging
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final TokenService tokenService;

    @Override
    @Auditable
    public User register(User user) {
        try {
            User _user = userService.createUser(user);
            log.debug("Registered user: " + user);
            return _user;
        } catch (UserAlreadyExistsException | UserIsAlreadyLoggedInException e) {
            log.debug("User with email {} is already logged in", user.getEmail());
            throw new ErrorRegistrationException("User with email " + user.getEmail() + " is already logged in");
        }
    }

    @Override
    @Auditable
    public ResponseLogin login(SignIn signin) {
        User user;

        try {
            user = userService.getUserByEmail(signin.email());

        } catch (UserException e) {
            log.debug("User with email {} not found", signin.email());
            throw new ErrorLoginExeption(e.getMessage());
        }


        if (signin.password().equals(user.getPassword())) {
            Integer key = new Random().nextInt(100);

            Token token = new Token.Builder()
                    .userId(user.getId())
                    .value(key.toString())
                    .build();
            tokenService.saveToken(token);

            UserContext.setCurrentUser(user);
            log.debug("Authenticated user: " + signin.email());
            return new ResponseLogin(user.getId().toString());
        }

        log.debug("Invalid password or email", signin.email());
        throw new ErrorLoginExeption("Invalid password or email");
    }

    @Override
    @Auditable
    public void logout() {
        app.entity.User user = (app.entity.User) UserContext.getCurrentUser();
        if (user == null)
            throw new ErrorLogoutException("You are not logged in");

        tokenService.deleteTokenByUserId(user.getId());

        UserContext.clear();
    }
}
