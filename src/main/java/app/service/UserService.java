package app.service;

import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.User;

import java.util.List;

public interface UserService {

    UserDto createUser(CreateUserDto createUserDto);

    UserDto updateDataUser(UpdateUserDto userDto, String email);


    void remove(String email);

    User getUserByEmail(String email);

    UserDto getDataUser(String email);

    List<UserDto> list();

    boolean blockUser (String email);
}
