package test.integration;

import app.App;
import app.config.LiquibaseConfig;
import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.User;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorLogoutException;
import app.exception.auth.ErrorRegistrationException;
import app.mapper.UserMapper;
import app.service.AuthService;
import neket27.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = App.class)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(LiquibaseConfig.class)
class AuthServiceIT {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private app.config.LiquibaseConfig liquibaseConfig;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgres.start();

        // Настройки для datasource
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Настройки для liquibase
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "db/test-changelog/changelog-master.yml");

        // Создание схем
        try (var connection = postgres.createConnection("")) {
            var statement = connection.createStatement();
            List<String> schemas = List.of("public", "metadata", "business");

            for (String schema : schemas)
                statement.execute("CREATE SCHEMA IF NOT EXISTS " + schema);

            for (String schema : schemas)
                statement.execute("SET search_path TO " + schema);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    @BeforeEach
//    void setup() {
//        TestDatabase database = TestDatabaseFactory.create();
//        TransactionService transactionService = new TransactionServiceImpl(new TransactionJdbcRepository(database.jdbcTemplate()), Mappers.getMapper(TransactionMapper.class));
//        FinanceService financeService = new FinanceServiceImpl(new FinanceJdbcRepository(database.jdbcTemplate()), transactionService, Mappers.getMapper(FinanceMapper.class));
//        UserService userService = new UserServiceImpl(Mappers.getMapper(UserMapper.class), new UserJdbcRepository(database.jdbcTemplate()), financeService);
//        TokenService tokenService = new TokenServiceImpl(new TokenJdbcRepository(database.jdbcTemplate()));
//        authService = new AuthServiceImpl(userService, tokenService);
//        userMapper = Mappers.getMapper(UserMapper.class);
//
//    }

    @BeforeEach
    void setUp() {

        // Создание схем
        try (var connection = postgres.createConnection("")) {
            var statement = connection.createStatement();
            List<String> schemas = List.of("public", "metadata", "business");

            for (String schema : schemas)
                statement.execute("CREATE SCHEMA IF NOT EXISTS " + schema);

            for (String schema : schemas)
                statement.execute("SET search_path TO " + schema);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @AfterEach
    void tearDown() {
        try (var connection = postgres.createConnection("")) {
            var statement = connection.createStatement();
            List<String> schemas = List.of("public", "metadata", "business");

            for (String schema : schemas)
                statement.execute("DROP SCHEMA " + schema + " CASCADE;");


        } catch (Exception e) {
            e.printStackTrace();
        }

        liquibaseConfig.initialize();
        UserContext.clear();
    }


    @Test
    void registerSuccess() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("name", "email@mail.ru", "password1234");
        User entity = userMapper.toEntity(createUserDto);
        // Act
        User user = authService.register(entity);
        UserDto userDto = userMapper.toDto(user);

        // Assert
        assertEquals(createUserDto.name(), userDto.name());
        assertEquals(createUserDto.email(), userDto.email());
        assertEquals(createUserDto.password(), userDto.password());
    }

    @Test
    void loginSuccess() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("name", "email@mail.ru", "password1234");
        User entity = userMapper.toEntity(createUserDto);
        User registeredUser = authService.register(entity);
        SignIn signIn = new SignIn(registeredUser.getEmail(), registeredUser.getPassword());

        // Act
        ResponseLogin response = authService.login(signIn);

        // Assert
        assertNotNull(response);
        assertEquals(registeredUser.getId().toString(), response.token());
    }

    @Test
    void loginFailure_WrongPassword() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("name", "email@mail.ru", "password1234");
        User entity = userMapper.toEntity(createUserDto);
        authService.register(entity);
        SignIn signIn = new SignIn(createUserDto.email(), "wrongpassword");

        // Act & Assert
        assertThrows(ErrorLoginExeption.class, () -> authService.login(signIn));
    }

    @Test
    void loginFailure_UserNotFound() {
        // Arrange
        SignIn signIn = new SignIn("nonexistent@mail.ru", "password1234");

        // Act & Assert
        assertThrows(ErrorLoginExeption.class, () -> authService.login(signIn));
    }

    @Test
    void logoutSuccess() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("name", "email@mail.ru", "password1234");
        User entity = userMapper.toEntity(createUserDto);
        User registeredUser = authService.register(entity);
        SignIn signIn = new SignIn(registeredUser.getEmail(), registeredUser.getPassword());
        authService.login(signIn);

        // Act & Assert
        assertDoesNotThrow(() -> authService.logout());
    }

    @Test
    void logoutFailure_NotLoggedIn() {
        // Act & Assert
        assertThrows(ErrorLogoutException.class, () -> authService.logout());
    }

    @Test
    void registerFailure_EmailAlreadyExists() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("name", "email@mail.ru", "password1234");
        User entity = userMapper.toEntity(createUserDto);
        authService.register(entity);

        // Act & Assert
        assertThrows(ErrorRegistrationException.class, () -> authService.register(entity));
    }

    @Test
    void loginFailure_EmptyPassword() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("name", "email@mail.ru", "password1234");
        User entity = userMapper.toEntity(createUserDto);
        authService.register(entity);
        SignIn signIn = new SignIn(createUserDto.email(), "");

        // Act & Assert
        assertThrows(ErrorLoginExeption.class, () -> authService.login(signIn));
    }

    @Test
    void loginFailure_EmptyEmail() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("name", "email@mail.ru", "password1234");
        User entity = userMapper.toEntity(createUserDto);
        authService.register(entity);
        SignIn signIn = new SignIn("", createUserDto.password());

        // Act & Assert
        assertThrows(ErrorLoginExeption.class, () -> authService.login(signIn));
    }

    @Test
    void logoutSuccess_AfterMultipleLogins() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("name", "email@mail.ru", "password1234");
        User entity = userMapper.toEntity(createUserDto);
        authService.register(entity);
        SignIn signIn = new SignIn(createUserDto.email(), createUserDto.password());
        authService.login(signIn);
        authService.login(signIn);

        // Act & Assert
        assertDoesNotThrow(() -> authService.logout());
    }


}
