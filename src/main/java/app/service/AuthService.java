package app.service;

import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;

public interface AuthService {

    boolean register(CreateUserDto userDto);

    boolean login(String email, String password);

    boolean logout();
}
