package test.integration;

import app.dto.user.CreateUserDto;
import app.exception.NotFoundException;
import app.exception.UserExistException;
import app.exception.UserIsAlreadyLoggedInException;
import app.service.AuthService;
import app.service.UserService;
import app.service.impl.AuthServiceImpl;
import app.auth.Authenticator;
import app.config.AuthenticationConfig;
import app.context.UserContext;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import test.db.TestDatabase;
import test.db.TestDatabaseFactory;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIT {

    private TestDatabase database;
    private AuthService authService;
    private UserService userService;
    private Authenticator authenticator;
    private AuthenticationConfig authenticationConfig;

    @BeforeEach
    void setup() {
        database = TestDatabaseFactory.create();

        authenticationConfig = new AuthenticationConfig(new HashMap<>());
        authenticator = spy(new Authenticator(authenticationConfig));
        userService = spy(new UserServiceImpl(
                new app.mapper.UserMapper(),
                new app.repository.jdbc.UserJdbcRepository(database.connection()),
                new app.repository.jdbc.FinanceJdbcRepository(database.connection()),
                new app.mapper.FinanceMapper()
        ));
        authService = new AuthServiceImpl(authenticationConfig, authenticator, userService);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        TestDatabaseFactory.reset();
    }

    @Test
    @Order(1)
    void testRegister_UserIsAlreadyLoggedInException() {
        // Arrange
        var userDto = new CreateUserDto("UserSix", "user6@example.com", "pass123");
        doThrow(new UserIsAlreadyLoggedInException("Already logged in"))
                .when(userService).createUser(any(CreateUserDto.class));

        // Act
        boolean result = authService.register(userDto);

        // Assert
        assertFalse(result);
        verify(userService, times(1)).createUser(userDto);
    }

    @Test
    @Order(2)
    void testRegister_UserExistException() {
        // Arrange
        var userDto = new CreateUserDto("UserSeven", "user7@example.com", "pass123");
        doThrow(new UserExistException("User exists"))
                .when(userService).createUser(any(CreateUserDto.class));

        // Act
        boolean result = authService.register(userDto);

        // Assert
        assertFalse(result);
        verify(userService, times(1)).createUser(userDto);
    }

    @Test
    @Order(3)
    void testLogin_UserNotFound() {
        // Arrange
        doThrow(new NotFoundException("Not found"))
                .when(userService).getUserByEmail("nonexistent@example.com");

        // Act
        boolean result = authService.login("nonexistent@example.com", "any");

        // Assert
        assertFalse(result);
        verify(userService, times(1)).getUserByEmail("nonexistent@example.com");
    }

    @Test
    @Order(4)
    void testLogin_AuthenticatorCheckCredentials_Success() {
        // Arrange
        var userDto = new CreateUserDto("UserEight", "user8@example.com", "mypass");
        authService.register(userDto);
        doReturn(true).when(authenticator).checkCredentials("user8@example.com", "mypass");

        // Act
        boolean result = authService.login("user8@example.com", "mypass");

        // Assert
        assertTrue(result);
        assertNotNull(UserContext.getCurrentUser());
        verify(authenticator, times(1)).checkCredentials("user8@example.com", "mypass");
    }

    @Test
    @Order(5)
    void testLogin_FallbackAuthenticationConfig_AddCredential() {
        // Arrange
        var userDto = new CreateUserDto("UserNine", "user9@example.com", "altpass");
        authService.register(userDto);

        doReturn(false).when(authenticator).checkCredentials("user9@example.com", "altpass");

        // Act
        boolean result = authService.login("user9@example.com", "altpass");

        // Assert
        assertTrue(result);
        assertNotNull(UserContext.getCurrentUser());
        assertTrue(authenticationConfig.hasCredential("user9@example.com"));
    }

    @Test
    @Order(6)
    void testLogin_InvalidPasswordAndFallbackFails() {
        // Arrange
        var userDto = new CreateUserDto("UserTen", "user10@example.com", "correctpass");
        authService.register(userDto);

        doReturn(false).when(authenticator).checkCredentials("user10@example.com", "wrongpass");

        // Act
        boolean result = authService.login("user10@example.com", "wrongpass");

        // Assert
        assertFalse(result);
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    @Order(7)
    void testLogout_NoUserInContext() {
        // Act
        boolean result = authService.logout();

        // Assert
        assertFalse(result);
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    @Order(8)
    void testLogout_WithUserInContext() {
        // Arrange
        var userDto = new CreateUserDto("UserEleven", "user11@example.com", "logoutpass");
        authService.register(userDto);
        authService.login("user11@example.com", "logoutpass");

        // Act
        boolean result = authService.logout();

        // Assert
        assertTrue(result);
        assertNull(UserContext.getCurrentUser());
    }
}
