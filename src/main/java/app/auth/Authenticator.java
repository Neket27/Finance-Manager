package app.auth;

import app.dto.user.UserDto;
import app.entity.Token;
import app.exception.TokenException;
import app.service.TokenService;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Authenticator {

    private static final Logger log = LoggerFactory.getLogger(Authenticator.class);
    private UserService userService;
    private final TokenService tokenService;

    public Authenticator(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    public UserDto authenticate(String _token) {
        try {
            Token token = tokenService.getTokenByUserId(Long.valueOf(_token));
            return userService.getUserById(token.getUserId());

        } catch (TokenException e) {
            log.error(e.getMessage());
            return null;
//            throw new TokenException("Token is invalid");
        }
    }

}
