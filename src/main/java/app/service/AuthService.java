package app.service;

import app.dto.user.CreateUserDto;

public interface AuthService {

    boolean register(CreateUserDto userDto);

    boolean login(String email, String password);

    boolean logout();
}
