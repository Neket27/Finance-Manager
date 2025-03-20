package test.integration;

import app.auth.Authenticator;
import app.config.AuthenticationConfig;
import app.context.UserContext;
import app.dto.user.CreateUserDto;
import app.mapper.FinanceMapper;
import app.mapper.UserMapper;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.UserJdbcRepository;
import app.service.AuthService;
import app.service.UserService;
import app.service.impl.AuthServiceImpl;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import test.db.TestDatabase;
import test.db.TestDatabaseFactory;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.Random.class)
class AuthServiceImplIT {

    private TestDatabase database;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setup() {
        database = TestDatabaseFactory.create();

        var authenticationConfig = new AuthenticationConfig(new HashMap<>());
        var authenticator = new Authenticator(authenticationConfig);
        var financeRepository = new FinanceJdbcRepository(database.connection());
        var financeMapper = new FinanceMapper();
        var userMapper = new UserMapper();
        var userRepository = new UserJdbcRepository(database.connection());

        userService = new UserServiceImpl(userMapper, userRepository, financeRepository, financeMapper);
        authService = new AuthServiceImpl(authenticationConfig, authenticator, userService);
    }


    @AfterEach
    void tearDown() {
        UserContext.clear();
        TestDatabaseFactory.reset();
    }

    @Test
    void testRegister_Success() {
        // Arrange
        var userDto = new CreateUserDto("User One", "user1@example.com", "pass123");

        // Act
        boolean result = authService.register(userDto);
        var user = userService.getUserByEmail("user1@example.com");

        // Assert
        assertTrue(result);
        assertNotNull(user);
        assertEquals("User One", user.name());
    }

    @Test
    void testRegister_Duplicate() {
        // Arrange
        var userDto = new CreateUserDto("User Two", "user2@example.com", "pass123");
        authService.register(userDto);

        // Act
        boolean result = authService.register(userDto);

        // Assert
        assertFalse(result);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        var userDto = new CreateUserDto("User Three", "user3@example.com", "secure123");
        authService.register(userDto);

        // Act
        boolean loginResult = authService.login("user3@example.com", "secure123");
        var currentUser = UserContext.getCurrentUser();

        // Assert
        assertTrue(loginResult);
        assertNotNull(currentUser);
        assertEquals("user3@example.com", currentUser.email());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        var userDto = new CreateUserDto("User Four", "user4@example.com", "rightpass");
        authService.register(userDto);

        // Act
        boolean loginResult = authService.login("user4@example.com", "wrongpass");

        // Assert
        assertFalse(loginResult);
    }

    @Test
    void testLogout() {
        // Arrange
        var userDto = new CreateUserDto("User Five", "user5@example.com", "mypassword");
        authService.register(userDto);
        authService.login("user5@example.com", "mypassword");

        // Act
        boolean logoutResult = authService.logout();
        var currentUser = UserContext.getCurrentUser();

        // Assert
        assertTrue(logoutResult);
        assertNull(currentUser);
    }
}
