package app.service;

import app.dto.auth.Signin;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;

public interface AuthService {

    UserDto register(CreateUserDto userDto);

    UserDto login(Signin signin);

    boolean logout();
}
