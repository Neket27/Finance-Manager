package app.auth;

import app.config.AuthenticationConfig;
import app.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Authenticator {

    private static final Logger log = LoggerFactory.getLogger(Authenticator.class);
    private final AuthenticationConfig authenticationConfig;

    public Authenticator(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    public boolean checkCredentials(String email, String password) {
        User user = authenticationConfig.getCredentials().get(email);
        if (user == null) {
            log.debug("User with email {} not found",email);
            return false;
        }
        return password.equals(user.getPassword());
    }
}
