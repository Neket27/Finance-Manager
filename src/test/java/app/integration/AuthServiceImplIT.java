package app.integration;

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
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.Random.class)
class AuthServiceImplIT {

    private PostgreSQLContainer<?> postgres;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setup() throws Exception {
        postgres = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(false);
        postgres.start();

        var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase(
                "db/changelog/changelog-master.yml",
                new ClassLoaderResourceAccessor(),
                database
        );
        liquibase.update();

        var authenticationConfig = new AuthenticationConfig(new HashMap<>());
        var authenticator = new Authenticator(authenticationConfig);
        var financeRepository = new FinanceJdbcRepository(connection);
        var financeMapper = new FinanceMapper();
        var userMapper = new UserMapper();
        var userRepository = new UserJdbcRepository(connection);

        userService = new UserServiceImpl(userMapper, userRepository, financeRepository, financeMapper);
        authService = new AuthServiceImpl(authenticationConfig, authenticator, userService);
    }

    @AfterEach
    void tearDown() {
        postgres.stop();
        UserContext.clear();
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
