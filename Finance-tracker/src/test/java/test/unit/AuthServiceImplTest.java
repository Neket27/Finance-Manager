package test.unit;

import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.User;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorLogoutException;
import app.exception.auth.ErrorRegistrationException;
import app.exception.user.UserAlreadyExistsException;
import app.exception.user.UserIsAlreadyLoggedInException;
import app.service.TokenService;
import app.service.UserService;
import app.service.impl.AuthServiceImpl;
import neket27.context.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private CreateUserDto createUserDto;
    private User user;
    private SignIn signInDto;

    @BeforeEach
    void setUp() {
        createUserDto = new CreateUserDto("name", "test@example.com", "password123");
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .build();

        signInDto = new SignIn("test@example.com", "password123");

        UserContext.setCurrentUser(null);
    }

    @Test
    void register_Success() {
        // Arrange
        when(userService.createUser(user)).thenReturn(user);
        // Act
        User result = authService.register(user);
        // Assert
        assertNotNull(result);
        assertEquals(user, result);
        verify(userService, times(1)).createUser(user);
    }

    @Test
    void register_UserAlreadyLoggedIn() {
        // Arrange
        doThrow(new UserIsAlreadyLoggedInException("User already logged in"))
                .when(userService).createUser(user);
        // Act & Assert
        ErrorRegistrationException exception = assertThrows(ErrorRegistrationException.class, () -> authService.register(user));
        assertEquals("User with email " + user.getEmail() + " is already logged in", exception.getMessage());
        verify(userService, times(1)).createUser(user);
    }

    @Test
    void register_UserExists() {
        // Arrange
        doThrow(new UserAlreadyExistsException("User already exists"))
                .when(userService).createUser(user);
        // Act & Assert
        ErrorRegistrationException exception = assertThrows(ErrorRegistrationException.class, () -> authService.register(user));
        assertEquals("User with email " + createUserDto.email() + " is already logged in", exception.getMessage());
        verify(userService, times(1)).createUser(user);
    }

    @Test
    void login_Success() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        // Act
        ResponseLogin result = authService.login(signInDto);
        // Assert
        assertNotNull(result);
        assertEquals(user.getId().toString(), result.token());
        assertEquals(user, UserContext.getCurrentUser());
        verify(tokenService, times(1)).saveToken(any());
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(userService.getUserByEmail("notfound@example.com")).thenThrow(new ErrorLoginExeption("User not found"));
        // Act & Assert
        ErrorLoginExeption exception = assertThrows(ErrorLoginExeption.class, () -> authService.login(new SignIn("notfound@example.com", "password123")));
        assertEquals("User not found", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        // Act & Assert
        ErrorLoginExeption exception = assertThrows(ErrorLoginExeption.class, () -> authService.login(new SignIn("test@example.com", "wrongpassword")));
        assertEquals("Invalid password or email", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void login_EmptyEmail() {
        // Arrange
        when(userService.getUserByEmail(anyString())).thenThrow(new ErrorLoginExeption("Invalid password or email"));

        // Act & Assert
        ErrorLoginExeption exception = assertThrows(ErrorLoginExeption.class, () -> authService.login(new SignIn("", "password123")));
        assertEquals("Invalid password or email", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void login_EmptyPassword() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        // Act & Assert
        ErrorLoginExeption exception = assertThrows(ErrorLoginExeption.class, () -> authService.login(new SignIn("test@example.com", "")));
        assertEquals("Invalid password or email", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void logout_Success() {
        // Arrange
        UserContext.setCurrentUser(user);
//        when(tokenService.deleteTokenByUserId(userDto.id())).thenReturn(true);
        doNothing().when(tokenService).deleteTokenByUserId(user.getId());

        // Act
        authService.logout();
        // Assert
        assertNull(UserContext.getCurrentUser());
        verify(tokenService, times(1)).deleteTokenByUserId(user.getId());
    }

    @Test
    void logout_NoUserLoggedIn() {
        // Arrange
        UserContext.setCurrentUser(null);
        // Act & Assert
        ErrorLogoutException exception = assertThrows(ErrorLogoutException.class, () -> authService.logout());
        assertEquals("You are not logged in", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
    }
}
