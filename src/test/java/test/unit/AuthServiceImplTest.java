package test.unit;

import app.auth.Authenticator;
import app.context.UserContext;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.exception.NotFoundException;
import app.exception.UserIsAlreadyLoggedInException;
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
    private Authenticator authenticator;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private CreateUserDto createUserDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        createUserDto = new CreateUserDto("name", "test@example.com", "password123");
        userDto = new UserDto.Builder()
                .email("test@example.com")
                .password("hashedPassword")
                .build();

        UserContext.setCurrentUser(null);
    }

    @Test
    void register_Success() {
        // Arrange
        when(userService.createUser(createUserDto)).thenReturn(userDto);

        // Act
        boolean result = authService.register(createUserDto);

        // Assert
        assertTrue(result);
        verify(userService, times(1)).createUser(createUserDto);
    }

    @Test
    void register_UserAlreadyLoggedIn() {
        // Arrange
        doThrow(new UserIsAlreadyLoggedInException("User  already logged in"))
                .when(userService).createUser(createUserDto);

        // Act
        boolean result = authService.register(createUserDto);

        // Assert
        assertFalse(result);
        verify(userService, times(1)).createUser(createUserDto);
    }

    @Test
    void login_Success() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(authenticator.checkCredentials("test@example.com", "password123")).thenReturn(true);

        // Act
        boolean result = authService.login("test@example.com", "password123");

        // Assert
        assertTrue(result);
        assertEquals(userDto, UserContext.getCurrentUser());
        verify(authenticator, times(1)).checkCredentials("test@example.com", "password123");
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(userService.getUserByEmail("notfound@example.com")).thenThrow(new NotFoundException("User  not found"));

        // Act
        boolean result = authService.login("notfound@example.com", "password123");

        // Assert
        assertFalse(result);
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);
        when(authenticator.checkCredentials("test@example.com", "wrongpassword")).thenReturn(false);

        // Act
        boolean result = authService.login("test@example.com", "wrongpassword");

        // Assert
        assertFalse(result);
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void logout_Success() {
        // Arrange
        UserContext.setCurrentUser(userDto);
        when(authenticator.clearCredentials("test@example.com")).thenReturn(true);

        // Act
        boolean result = authService.logout();

        // Assert
        assertTrue(result);
        assertNull(UserContext.getCurrentUser());
        verify(authenticator, times(1)).clearCredentials("test@example.com");
    }
}
