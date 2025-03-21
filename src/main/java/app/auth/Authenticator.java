package app.auth;

import app.config.AuthenticationConfig;
import app.dto.user.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Authenticator {

    private static final Logger log = LoggerFactory.getLogger(Authenticator.class);
    private final AuthenticationConfig authenticationConfig;

    public Authenticator(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    public boolean checkCredentials(String email, String password) {
        UserDto user = authenticationConfig.getCredentials().get(email);
        if (user == null)
            return false;

        return password.equals(user.password());
    }

    public boolean clearCredentials(String email) {
        if (email == null)
            return false;

        try {
            authenticationConfig.getCredentials().remove(email);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
