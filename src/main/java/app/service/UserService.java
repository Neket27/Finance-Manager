package app.service;

import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.Role;

import java.util.List;

public interface UserService {

    UserDto createUser(CreateUserDto createUserDto);

    UserDto updateDataUser(UpdateUserDto userDto, String email);

    boolean remove(String email);

    UserDto getUserByEmail(String email);

    List<UserDto> list();

    boolean blockUser (String email);

    boolean changeUserRole(String email, Role role);
}
