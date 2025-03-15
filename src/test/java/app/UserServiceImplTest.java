package app;

import app.dto.finance.CreateFinanceDto;
import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.dto.user.UserDto;
import app.entity.Finance;
import app.entity.Role;
import app.entity.User;
import app.exception.NotFoundException;
import app.exception.UserExistException;
import app.mapper.FinanceMapper;
import app.mapper.UserMapper;
import app.repository.FinanceRepository;
import app.repository.UserRepository;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FinanceRepository financeRepository;

    @Mock
    private FinanceMapper financeMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserDto createUserDto;
    private UpdateUserDto updateUserDto;
    private UserDto userDto;
    private User user;
    private Finance finance;

    @BeforeEach
    void setUp() {
        createUserDto = new CreateUserDto("name", "test@example.com", "password123");
        updateUserDto = new UpdateUserDto("name", "test@example.com", "newPassword123", Role.User, 1L);
        userDto = new UserDto.Builder()
                .email("test@example.com")
                .password("newPassword123")
                .build();

        user = new User.Builder()
                .email("test@example.com")
                .role(Role.User)
                .finance(1L)
                .build();


        finance = new Finance.Builder()
                .id(1L)
                .build();
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.getAll()).thenReturn(new ArrayList<>());
        when(userMapper.toEntity(createUserDto)).thenReturn(user);
        when(financeMapper.toEntity(any(CreateFinanceDto.class))).thenReturn(finance);
        when(financeRepository.save(finance)).thenReturn(finance);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.createUser(createUserDto);

        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_UserAlreadyExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(UserExistException.class, () -> userService.createUser(createUserDto));
    }

    @Test
    void updateDataUser_Success() {
        when(userRepository.findById("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.updateEntity(updateUserDto, user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.updateDataUser(updateUserDto, "test@example.com");

        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userRepository, times(1)).findById("test@example.com");
    }

    @Test
    void updateDataUser_UserNotFound() {
        when(userRepository.findById("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateDataUser(updateUserDto, "notfound@example.com"));
    }

    @Test
    void remove_Success() {
        when(userRepository.findById("test@example.com")).thenReturn(Optional.of(user));

        boolean result = userService.remove("test@example.com");

        assertTrue(result);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void remove_UserNotFound() {
        when(userRepository.findById("notfound@example.com")).thenReturn(Optional.empty());

        boolean result = userService.remove("notfound@example.com");

        assertFalse(result);
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findById("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(userDto, result);
    }

    @Test
    void getUserByEmail_UserNotFound() {
        when(userRepository.findById("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserByEmail("notfound@example.com"));
    }

    @Test
    void listUsers_Success() {
        List<User> users = List.of(user);
        List<UserDto> userDtos = List.of(userDto);

        when(userRepository.getAll()).thenReturn(users);
        when(userMapper.toListDto(users)).thenReturn(userDtos);

        List<UserDto> result = userService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
    }

    @Test
    void blockUser_Success() {
        when(userRepository.findById("test@example.com")).thenReturn(Optional.of(user));

        boolean result = userService.blockUser("test@example.com");

        assertTrue(result);
        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void blockUser_UserNotFound() {
        when(userRepository.findById("notfound@example.com")).thenReturn(Optional.empty());

        boolean result = userService.blockUser("notfound@example.com");

        assertFalse(result);
    }

    @Test
    void changeUserRole_Success() {
        when(userRepository.findById("test@example.com")).thenReturn(Optional.of(user));

        boolean result = userService.changeUserRole("test@example.com", Role.Admin);

        assertTrue(result);
        assertEquals(Role.Admin, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeUserRole_UserNotFound() {
        when(userRepository.findById("notfound@example.com")).thenReturn(Optional.empty());

        boolean result = userService.changeUserRole("notfound@example.com", Role.Admin);

        assertFalse(result);
    }
}
