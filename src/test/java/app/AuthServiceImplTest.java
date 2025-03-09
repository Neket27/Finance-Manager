package app;

import app.auth.Authenticator;
import app.config.AuthenticationConfig;
import app.context.UserContext;
import app.dto.user.CreateUserDto;
import app.entity.User;
import app.exeption.UserIsAlreadyLoggedInExeption;
import app.exeption.UserNotFoundException;
import app.mapper.UserMapper;
import app.service.impl.AuthServiceImpl;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private AuthServiceImpl authService;
    private AuthenticationConfig authenticationConfig;
    private Authenticator authenticator;
    private UserMapper userMapper;
    private UserServiceImpl userService;


    @BeforeEach
    void setUp() {
        authenticationConfig = mock(AuthenticationConfig.class);
        authenticator = mock(Authenticator.class);
        userMapper = mock(UserMapper.class);
        userService = mock(UserServiceImpl.class);
        authService = new AuthServiceImpl(authenticationConfig, authenticator, userMapper, userService);
    }

    @BeforeEach
    void beforeAll() {
        UserContext.clear();
    }

    @Test
    void testRegisterSuccess() {
        CreateUserDto userDto = new CreateUserDto("name", "test@example.com", "password");

        when(userService.createUser(userDto)).thenReturn(null); // Assuming createUser  doesn't return anything

        boolean result = authService.register(userDto);

        assertTrue(result);
        verify(userService).createUser(userDto);
    }

    @Test
    void testRegisterUserAlreadyLoggedIn() {
        CreateUserDto userDto = new CreateUserDto("name", "test@example.com", "password");

        doThrow(new UserIsAlreadyLoggedInExeption("User is already logged ")).when(userService).createUser(userDto);

        boolean result = authService.register(userDto);

        assertFalse(result);
        verify(userService).createUser(userDto);
    }

    @Test
    void testLoginSuccess() {
        String username = "name";
        String email = "test@example.com";
        String password = "password";
        User user = new User.Builder().setName(username).setEmail(email).password(password).build();

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(authenticator.checkCredentials(email, password)).thenReturn(false);

        boolean result = authService.login(email, password);

        assertTrue(result);
        verify(authenticationConfig).addCredential(user);
        assertEquals(user, UserContext.getCurrentUser());
    }

    @Test
    void testLoginSuccess2() {
        String username = "name";
        String email = "test@example.com";
        String password = "password";
        User user = new User.Builder().setName(username).setEmail(email).password(password).build();

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(authenticator.checkCredentials(email, password)).thenReturn(true);

        boolean result = authService.login(email, password);

        assertTrue(result);
        verify(authenticationConfig, never()).addCredential(user);
        assertEquals(user, UserContext.getCurrentUser());
    }

    @Test
    void testLoginCredentials() {
        String username = "name";
        String email = "test@example.com";
        String password = "wrongpassword";
        User user = new User.Builder().setName(username).setEmail(email).password(password).build();

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(authenticator.checkCredentials(email, password)).thenReturn(false);

        boolean result = authService.login(email, password);

        assertTrue(result);
        verify(authenticationConfig).addCredential(user);
        assertEquals(user, UserContext.getCurrentUser());
    }

    @Test
    void testLoginUserNotFound() {
        String email = "notfound@example.com";
        String password = "password";

        when(userService.getUserByEmail(email)).thenThrow(new UserNotFoundException(String.format("User with email %s not found", email)));

        boolean result = authService.login(email, password);

        assertFalse(result);
        verify(authenticationConfig, never()).addCredential(any());
        assertNull(UserContext.getCurrentUser());
    }
}
