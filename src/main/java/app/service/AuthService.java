package app.service;

import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;

public interface AuthService {

    UserDto register(CreateUserDto userDto);

    ResponseLogin login(SignIn signIn);

    void logout();
}
