package app.service.impl;

import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.Role;
import app.entity.User;
import app.exeption.UserExistException;
import app.exeption.UserNotFoundException;
import app.mapper.UserMapper;
import app.repository.UserRepository;
import app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    @Override
    public UserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByEmail(createUserDto.email()))
            throw new UserExistException(String.format("User with email %s already exists", createUserDto.email()));

        User user = userMapper.toEntity(createUserDto);
        user.setFinance(new Finance(new ArrayList<>()));

        if (userRepository.getAll().isEmpty())
            user.setRole(Role.Admin);
        else
            user.setRole(Role.User);

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto updateDataUser(UpdateUserDto userDto, String email) {
        if (userDto == null)
            throw new IllegalArgumentException("User  Dto не может быть null");

        User user = getUserByEmail(email);
        user = userMapper.updateEntity(userDto, user);
        return userMapper.toDto(user);
    }

    @Override
    public void remove(String email) {
        userRepository.delete(this.getUserByEmail(email));
        log.debug("User {} removed", email);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with email %s not found", email)));
    }

    @Override
    public UserDto getDataUser(String email) {
        return userMapper.toDto(getUserByEmail(email));
    }

    @Override
    public List<UserDto> list() {
        return userMapper.toListDto(userRepository.getAll());
    }

    @Override
    public boolean blockUser(String email) {
        List<UserDto> userDtoList = this.list();
        for (UserDto user : userDtoList) {
            if (user.email().equals(email)) {
                User u = getUserByEmail(email);
                u.deactivate();
                userRepository.save(u);
                log.debug("Пользователь {} заблокирован.", email);
                return true;
            }
        }
        log.debug("Пользователь с email {} не найден.", email);
        return false;
    }

}
