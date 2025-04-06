package test.integration;

import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.User;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorLogoutException;
import app.exception.auth.ErrorRegistrationException;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.mapper.UserMapper;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.TokenJdbcRepository;
import app.repository.jdbc.TransactionJdbcRepository;
import app.repository.jdbc.UserJdbcRepository;
import app.service.*;
import app.service.impl.*;
import neket27.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import test.integration.db.TestDatabase;
import test.integration.db.TestDatabaseFactory;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceIT {

    private AuthService authService;
    private UserMapper userMapper;

    @BeforeEach
    void setup() {
        TestDatabase database = TestDatabaseFactory.create();
        TransactionService transactionService = new TransactionServiceImpl(new TransactionJdbcRepository(database.jdbcTemplate()), Mappers.getMapper(TransactionMapper.class));
        FinanceService financeService = new FinanceServiceImpl(new FinanceJdbcRepository(database.jdbcTemplate()), transactionService, Mappers.getMapper(FinanceMapper.class));
        UserService userService = new UserServiceImpl(Mappers.getMapper(UserMapper.class), new UserJdbcRepository(database.jdbcTemplate()), financeService);
        TokenService tokenService = new TokenServiceImpl(new TokenJdbcRepository(database.jdbcTemplate()));
        authService = new AuthServiceImpl(userService, tokenService);
        userMapper = Mappers.getMapper(UserMapper.class);

    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        TestDatabaseFactory.reset();
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
