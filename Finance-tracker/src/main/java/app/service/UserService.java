package app.service;

import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.Role;
import app.entity.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    User updateDataUser(User user, String email);

    boolean remove(String email);

    User getUserByEmail(String email);

    List<User> list();

    boolean blockUser (String email);

    boolean changeUserRole(String email, Role role);

    User getUserById(Long id);
}
