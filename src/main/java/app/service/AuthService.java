package app.service;

import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.entity.User;

public interface AuthService {

    User register(User user);

    ResponseLogin login(SignIn signIn);

    void logout();
}
