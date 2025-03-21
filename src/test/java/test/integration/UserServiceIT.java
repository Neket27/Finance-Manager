package test.integration;

import app.context.UserContext;
import app.dto.user.CreateUserDto;
import app.dto.user.UpdateUserDto;
import app.entity.Role;
import app.mapper.FinanceMapper;
import app.mapper.UserMapper;
import app.repository.FinanceRepository;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.UserJdbcRepository;
import app.service.UserService;
import app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.db.TestDatabase;
import test.db.TestDatabaseFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceIT {

    private TestDatabase database;
    private UserService userService;

    @BeforeEach
    void setup() {
        database = TestDatabaseFactory.create();
        FinanceRepository financeRepository = new FinanceJdbcRepository(database.connection());
        FinanceMapper financeMapper = new FinanceMapper();
        UserMapper userMapper = new UserMapper();
        UserJdbcRepository userRepository = new UserJdbcRepository(database.connection());

        userService = new UserServiceImpl(userMapper, userRepository, financeRepository, financeMapper);
    }

    @AfterEach
    void tearDown() {
        TestDatabaseFactory.reset();
    }

    @Test
    void testCreateUser_NullDto_ShouldThrowException() {
        var exception = assertThrows(NullPointerException.class, () -> {
            userService.createUser(null);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("null"));
    }

    @Test
    void testUpdateUser_NullDto_ShouldThrowIllegalArgumentException() {
        userService.createUser(new CreateUserDto("Dummy", "dummy@example.com", "pass"));
        var ex = assertThrows(IllegalArgumentException.class, () -> userService.updateDataUser(null, "dummy@example.com"));
        assertEquals("User Dto не может быть null", ex.getMessage());
    }

    @Test
    void testUpdateNonExistingUser_ShouldThrowNotFoundException() {
        var updateDto = new UpdateUserDto.Builder()
                .name("Ghost")
                .email("ghost@example.com")
                .password("pass")
                .build();
        var ex = assertThrows(Exception.class, () -> userService.updateDataUser(updateDto, "ghost@example.com"));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void testRemoveNonExistingUser_ShouldReturnFalse() {
        boolean removed = userService.remove("unknown@example.com");
        assertFalse(removed);
    }

    @Test
    void testBlockNonExistingUser_ShouldReturnFalse() {
        boolean result = userService.blockUser("nonexistent@example.com");
        assertFalse(result);
    }

    @Test
    void testChangeUserRole_InvalidUser_ShouldReturnFalse() {
        boolean result = userService.changeUserRole("ghost@example.com", Role.ADMIN);
        assertFalse(result);
    }

    @Test
    void testCreateUser_CheckFinanceIsNotNull() {
        var userDto = userService.createUser(new CreateUserDto("Finance Check", "finance@example.com", "pass"));
        assertNotNull(userDto.financeId());
    }

    @Test
    void testChangeUserRole_ShouldSetUserContext() {
        userService.createUser(new CreateUserDto("Clark Kent", "superman@example.com", "kryptonite"));
        boolean changed = userService.changeUserRole("superman@example.com", Role.ADMIN);
        assertTrue(changed);
        assertNotNull(UserContext.getCurrentUser());
        assertEquals("superman@example.com", UserContext.getCurrentUser().email());
    }

    @Test
    void testListUsers_WhenEmpty() {
        var users = userService.list();
        assertEquals(0, users.size());
    }

    @Test
    void testListUsers_AfterDeletion() {
        userService.createUser(new CreateUserDto("Temp User", "temp@example.com", "pass"));
        userService.remove("temp@example.com");
        List<?> users = userService.list();
        assertEquals(0, users.size());
    }

    @Test
    void testCreateMultipleAdmins_NotPossible() {
        userService.createUser(new CreateUserDto("Admin1", "admin1@example.com", "pass"));
        var user = userService.createUser(new CreateUserDto("Admin2", "admin2@example.com", "pass"));
        assertEquals(Role.USER, user.role());
    }
}
