package test.unit;

import app.context.UserContext;
import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorLogoutException;
import app.exception.auth.ErrorRegistrationException;
import app.exception.user.UserAlreadyExistsException;
import app.exception.user.UserIsAlreadyLoggedInException;
import app.service.TokenService;
import app.service.UserService;
import app.service.impl.AuthServiceImpl;
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
    private UserDto userDto;
    private SignIn signInDto;

    @BeforeEach
    void setUp() {
        createUserDto = new CreateUserDto("name", "test@example.com", "password123");
        userDto = new UserDto.Builder()
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
        when(userService.createUser(createUserDto)).thenReturn(userDto);
        // Act
        UserDto result = authService.register(createUserDto);
        // Assert
        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userService, times(1)).createUser(createUserDto);
    }

    @Test
    void register_UserAlreadyLoggedIn() {
        // Arrange
        doThrow(new UserIsAlreadyLoggedInException("User already logged in"))
                .when(userService).createUser(createUserDto);
        // Act & Assert
        ErrorRegistrationException exception = assertThrows(ErrorRegistrationException.class, () -> authService.register(createUserDto));
        assertEquals("User with email " + createUserDto.email() + " is already logged in", exception.getMessage());
        verify(userService, times(1)).createUser(createUserDto);
    }

    @Test
    void register_UserExists() {
        // Arrange
        doThrow(new UserAlreadyExistsException("User already exists"))
                .when(userService).createUser(createUserDto);
        // Act & Assert
        ErrorRegistrationException exception = assertThrows(ErrorRegistrationException.class, () -> authService.register(createUserDto));
        assertEquals("User with email " + createUserDto.email() + " is already logged in", exception.getMessage());
        verify(userService, times(1)).createUser(createUserDto);
    }

    @Test
    void login_Success() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        // Act
        ResponseLogin result = authService.login(signInDto);
        // Assert
        assertNotNull(result);
        assertEquals(userDto.id().toString(), result.token());
        assertEquals(userDto, UserContext.getCurrentUser());
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
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
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
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        // Act & Assert
        ErrorLoginExeption exception = assertThrows(ErrorLoginExeption.class, () -> authService.login(new SignIn("test@example.com", "")));
        assertEquals("Invalid password or email", exception.getMessage());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void logout_Success() {
        // Arrange
        UserContext.setCurrentUser(userDto);
//        when(tokenService.deleteTokenByUserId(userDto.id())).thenReturn(true);
        doNothing().when(tokenService).deleteTokenByUserId(userDto.id());

        // Act
        authService.logout();
        // Assert
        assertNull(UserContext.getCurrentUser());
        verify(tokenService, times(1)).deleteTokenByUserId(userDto.id());
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
