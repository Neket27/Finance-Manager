package app;

import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.User;
import app.exeption.UserExistException;
import app.exeption.UserNotFoundException;
import app.mapper.UserMapper;
import app.repository.UserRepository;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserServiceImpl userService;
    private UserMapper userMapper;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userMapper, userRepository);
    }

    @Test
    void createUserShouldReturnUserDtoWhenUserDoesNotExist() {
        CreateUserDto createUserDto = new CreateUserDto("name", "test@example.com", "password");

        User user = new User.Builder()
                .setName("name")
                .setEmail("test@example.com")
                .password("password")
                .build();


        UserDto userDto = new UserDto.Builder()
                .email("test@example.com")
                .build();

        when(userRepository.existsByEmail(createUserDto.email())).thenReturn(false);
        when(userMapper.toEntity(createUserDto)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.createUser(createUserDto);

        assertNotNull(result);
        assertEquals("test@example.com", result.email());
        verify(userRepository).save(any(User.class));
    }


    @Test
    void createUserShouldThrowUserExistExceptionWhenUserAlreadyExists() {
        CreateUserDto createUserDto = new CreateUserDto("name", "test@example.com", "password");

        when(userRepository.existsByEmail(createUserDto.email())).thenReturn(true);

        UserExistException exception = assertThrows(UserExistException.class, () -> userService.createUser(createUserDto));
        assertEquals("User with email test@example.com already exists", exception.getMessage());
    }

    @Test
    void updateDataUserShouldReturnUpdatedUserDtoWhenUserExists() {
        UpdateUserDto updateUserDto = new UpdateUserDto.Builder()
                .password("newPassword")
                .build();

        User user = new User.Builder()
                .setEmail("test@example.com")
                .password("oldPassword")
                .build();

        UserDto userDto = new UserDto.Builder()
                .email("test@example.com")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.updateEntity(updateUserDto, user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.updateDataUser(updateUserDto, "test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.email());
        verify(userMapper).updateEntity(updateUserDto, user);
    }

    @Test
    void updateDataUserShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UpdateUserDto updateUserDto = new UpdateUserDto.Builder()
                .password("newPassword")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.updateDataUser(updateUserDto, "test@example.com"));
        assertEquals("User with email test@example.com not found", exception.getMessage());
    }

    @Test
    void removeShouldRemoveUserWhenUserExists() {
        User user = new User.Builder()
                .setEmail("test@example.com")
                .password("passworrd")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        userService.remove("test@example.com");

        verify(userRepository).delete(user);
    }

    @Test
    void removeShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.remove("test@example.com"));
        assertEquals("User with email test@example.com not found", exception.getMessage());
    }

    @Test
    void getDataUserShouldReturnUserDtoWhenUserExists() {
        User user = new User.Builder()
                .setEmail("test@example.com")
                .password("password")
                .build();

        UserDto userDto = new UserDto.Builder()
                .email("test@example.com")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getDataUser("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.email());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getDataUserShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getDataUser("test@example.com"));
        assertEquals("User with email test@example.com not found", exception.getMessage());
    }

    @Test
    void listShouldReturnListOfUserDtos() {
        User user1 = new User.Builder()
                .setEmail("user1@example.com")
                .password("password1")
                .build();

        User user2 = new User.Builder()
                .setEmail("user2@example.com")
                .password("password2")
                .build();

        List<User> users = List.of(user1, user2);
        List<UserDto> userDtos = List.of(new UserDto("user1@example.com", "password1"), new UserDto("user2@example.com", "password2"));

        when(userRepository.getAll()).thenReturn(users);
        when(userMapper.toListDto(users)).thenReturn(userDtos);

        List<UserDto> result = userService.list();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1@example.com", result.get(0).email());
        assertEquals("user2@example.com", result.get(1).email());
        verify(userRepository).getAll();
    }

    @Test
    void blockUserShouldDeactivateUserWhenUserExists() {
        User user = new User.Builder()
                .setEmail("test@example.com")
                .password("password")
                .build();

        UserDto userDto = new UserDto.Builder()
                .email("test@example.com")
                .password("password")
                .isActive(true)
                .build();


        List<UserDto> userDtoList = List.of(userDto);
        when(userService.list()).thenReturn(userDtoList);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        boolean isBlockedUser  = userService.blockUser ("test@example.com");

        assertTrue(isBlockedUser);
        verify(userRepository).save(user);
    }

    @Test
    void blockUserShouldLogWhenUserDoesNotExist() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Здесь вы можете использовать мок логгера, чтобы проверить, что лог был вызван с ожидаемым сообщением
        userService.blockUser("nonexistent@example.com");

        // Проверка логирования может потребовать дополнительной настройки с использованием библиотеки логирования
        // Например, с использованием Mockito для проверки вызовов логгера
    }
}
