package test.integration;

import app.context.UserContext;
import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.entity.Role;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.mapper.UserMapper;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.TransactionJdbcRepository;
import app.repository.jdbc.UserJdbcRepository;
import app.service.FinanceService;
import app.service.TransactionService;
import app.service.UserService;
import app.service.impl.FinanceServiceImpl;
import app.service.impl.TransactionServiceImpl;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import test.integration.db.TestDatabase;
import test.integration.db.TestDatabaseFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceIT {

    private UserService userService;

    @BeforeEach
    void setup() {
        TestDatabase database = TestDatabaseFactory.create();
        TransactionService transactionService = new TransactionServiceImpl(new TransactionJdbcRepository(database.jdbcTemplate()), Mappers.getMapper(TransactionMapper.class));
        FinanceService financeService = new FinanceServiceImpl(new FinanceJdbcRepository(database.jdbcTemplate()), transactionService, Mappers.getMapper(FinanceMapper.class));
        userService = new UserServiceImpl(Mappers.getMapper(UserMapper.class), new UserJdbcRepository(database.jdbcTemplate()), financeService);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
        TestDatabaseFactory.reset();
    }

    @Test
    void testCreateUser_NullDto_ShouldThrowException() {
        // Act
        var exception = assertThrows(NullPointerException.class, () -> {
            userService.createUser(null);
        });

        // Assert
        assertTrue(exception.getMessage().toLowerCase().contains("null"));
    }

    @Test
    void testUpdateUser_NullDto_ShouldThrowIllegalArgumentException() {
        // Arrange
        userService.createUser(new CreateUserDto("Dummy", "dummy@example.com", "pass"));

        // Act
        var ex = assertThrows(IllegalArgumentException.class, () -> userService.updateDataUser(null, "dummy@example.com"));

        // Assert
        assertEquals("User Dto не может быть null", ex.getMessage());
    }

    @Test
    void testUpdateNonExistingUser_ShouldThrowNotFoundException() {
        // Arrange
        var updateDto = new UpdateUserDto.Builder()
                .name("Ghost")
                .email("ghost@example.com")
                .password("pass")
                .build();

        // Act
        var ex = assertThrows(Exception.class, () -> userService.updateDataUser(updateDto, "ghost@example.com"));

        // Assert
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void testRemoveNonExistingUser_ShouldReturnFalse() {
        // Act
        boolean removed = userService.remove("unknown@example.com");

        // Assert
        assertFalse(removed);
    }

    @Test
    void testBlockNonExistingUser_ShouldReturnFalse() {
        // Act
        boolean result = userService.blockUser("nonexistent@example.com");

        // Assert
        assertFalse(result);
    }

    @Test
    void testChangeUserRole_InvalidUser_ShouldReturnFalse() {
        // Act
        boolean result = userService.changeUserRole("ghost@example.com", Role.ADMIN);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCreateUser_CheckFinanceIsNotNull() {
        // Act
        var userDto = userService.createUser(new CreateUserDto("Finance Check", "finance@example.com", "pass"));

        // Assert
        assertNotNull(userDto.financeId());
    }

    @Test
    void testChangeUserRole_ShouldSetUserContext() {
        // Arrange
        userService.createUser(new CreateUserDto("Clark Kent", "superman@example.com", "kryptonite"));

        // Act
        boolean changed = userService.changeUserRole("superman@example.com", Role.ADMIN);

        // Assert
        assertTrue(changed);
        assertNotNull(UserContext.getCurrentUser());
        assertEquals("superman@example.com", UserContext.getCurrentUser().email());
    }

    @Test
    void testListUsers_WhenEmpty() {
        // Act
        var users = userService.list();

        // Assert
        assertEquals(0, users.size());
    }

    @Test
    void testListUsers_AfterDeletion() {
        // Arrange
        userService.createUser(new CreateUserDto("Temp User", "temp@example.com", "pass"));
        userService.remove("temp@example.com");

        // Act
        List<?> users = userService.list();

        // Assert
        assertEquals(0, users.size());
    }

    @Test
    void testCreateMultipleAdmins_NotPossible() {
        // Arrange
        userService.createUser(new CreateUserDto("Admin1", "admin1@example.com", "pass"));

        // Act
        var user = userService.createUser(new CreateUserDto("Admin2", "admin2@example.com", "pass"));

        // Assert
        assertEquals(Role.USER, user.role());
    }

    @Test
    void testRemoveUser_ShouldReturnTrueIfUserRemoved() {
        // Arrange
        userService.createUser(new CreateUserDto("Jane Doe", "jane@example.com", "pass"));

        // Act
        boolean removed = userService.remove("jane@example.com");

        // Assert
        assertTrue(removed);
    }

    @Test
    void testBlockUser_ShouldReturnTrueIfUserBlocked() {
        // Arrange
        userService.createUser(new CreateUserDto("Blocked User", "blocked@example.com", "pass"));

        // Act
        boolean result = userService.blockUser("blocked@example.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void testListUsers_ShouldReturnCorrectUsers() {
        // Arrange
        userService.createUser(new CreateUserDto("User 1", "user1@example.com", "pass"));
        userService.createUser(new CreateUserDto("User 2", "user2@example.com", "pass"));

        // Act
        List<?> users = userService.list();

        // Assert
        assertEquals(2, users.size());
    }

}
