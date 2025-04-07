package test.unit;

import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.Role;
import app.entity.User;
import app.mapper.UserMapper;
import app.repository.UserRepository;
import app.service.FinanceService;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FinanceService financeService;

    @Mock
    UserMapper userMapper;

    UserDto userDto;
    User user;

    @BeforeEach
    void setUp() {
        this.userDto = new UserDto(1L, "name", "email@mail.ru", "password1234", true, Role.USER, 1L);
        this.user = new User(1L, "name", "email@mail.ru", "password1234", true, Role.USER, 1L);
    }

    @Test
    void createUser() {
        CreateUserDto createUserDto = new CreateUserDto("name", "email@mail.ru", "password1234");

        when(userRepository.existsByEmail(createUserDto.email())).thenReturn(false);
        when(financeService.createEmptyFinance(any())).thenReturn(userDto.id());
        when(userRepository.save(user)).thenReturn(user);

        User returnUser = userService.createUser(user);

        assertEquals(user, returnUser);

    }

    @Test
    void updateDataUser() {
        User updatedUser = new User(1L, "name", "newemail@mail.ru", "newpassword1234", true, Role.USER, 1L);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.updateEntity(any(), any())).thenReturn(updatedUser);

        User returnUser = userService.updateDataUser(updatedUser, user.getEmail());

        assertEquals(updatedUser, returnUser);
    }

    @Test
    void remove() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        boolean result = userService.remove(user.getEmail());

        assertTrue(result);
    }

    @Test
    void getUserByEmail() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByEmail(user.getEmail());

        assertEquals(user, foundUser);
    }

    @Test
    void list() {
        List<User> users = List.of(user);

        when(userRepository.getAll()).thenReturn(users);
        when(userMapper.toList(users)).thenReturn(users);

        List<User> result = userService.list();

        assertEquals(users, result);
    }

    @Test
    void blockUser() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        boolean result = userService.blockUser(user.getEmail());

        assertFalse(user.isActive());
        assertTrue(result);
    }

    @Test
    void changeUserRole() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        boolean result = userService.changeUserRole(user.getEmail(), Role.ADMIN);

        assertEquals(Role.ADMIN, user.getRole());
        assertTrue(result);
    }

    @Test
    void getUserById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = userService.getUserById(user.getId());

        assertEquals(user, result);
    }

}