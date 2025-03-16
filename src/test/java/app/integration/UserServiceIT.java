package app.integration;

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
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceIT {

    private PostgreSQLContainer<?> postgres;
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

        FinanceRepository financeRepository = new FinanceJdbcRepository(connection);
        FinanceMapper financeMapper = new FinanceMapper();
        UserMapper userMapper = new UserMapper();
        UserJdbcRepository userRepository = new UserJdbcRepository(connection);

        userService = new UserServiceImpl(userMapper, userRepository, financeRepository, financeMapper);
    }

    @AfterEach
    void tearDown() {
        postgres.stop();
    }

    @Test
    @Order(1)
    void testCreateFirstUser_ShouldBeAdmin() {
        var createUserDto = new CreateUserDto("John Doe", "john@example.com", "password123");

        var userDto = userService.createUser(createUserDto);

        assertNotNull(userDto);
        assertEquals(Role.Admin, userDto.role());
        assertEquals("John Doe", userDto.name());
        assertTrue(userDto.isActive());
        assertNotNull(userDto.financeId());
    }

    @Test
    @Order(2)
    void testCreateSecondUser_ShouldBeUser() {
        userService.createUser(new CreateUserDto("Admin User", "admin@example.com", "adminpass"));

        var secondUserDto = new CreateUserDto("Jane Doe", "jane@example.com", "password");
        var userDto = userService.createUser(secondUserDto);

        assertNotNull(userDto);
        assertEquals(Role.User, userDto.role());
        assertEquals("Jane Doe", userDto.name());
    }

    @Test
    @Order(3)
    void testUpdateUser() {
        userService.createUser(new CreateUserDto("Mark Smith", "mark@example.com", "password"));

        var updateDto = new UpdateUserDto.Builder()
                .name("Mark Updated")
                .email("mark@example.com")
                .password("newpassword")
                .build();

        var updatedUser = userService.updateDataUser(updateDto, "mark@example.com");

        assertEquals("Mark Updated", updatedUser.name());
        assertEquals("mark@example.com", updatedUser.email());
    }

    @Test
    @Order(4)
    void testRemoveUser() {
        userService.createUser(new CreateUserDto("Lara Croft", "lara@example.com", "password"));

        boolean removed = userService.remove("lara@example.com");

        assertTrue(removed);
        var exception = assertThrows(Exception.class, () -> userService.getUserByEmail("lara@example.com"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @Order(5)
    void testBlockUser() {
        userService.createUser(new CreateUserDto("Clark Kent", "clark@example.com", "password"));

        boolean blocked = userService.blockUser("clark@example.com");

        assertTrue(blocked);
        var user = userService.getUserByEmail("clark@example.com");
        assertFalse(user.isActive());
    }

    @Test
    @Order(6)
    void testChangeUserRole() {
        userService.createUser(new CreateUserDto("Bruce Wayne", "bruce@example.com", "password"));

        boolean changed = userService.changeUserRole("bruce@example.com", Role.Admin);

        assertTrue(changed);
        var user = userService.getUserByEmail("bruce@example.com");
        assertEquals(Role.Admin, user.role());
    }

    @Test
    @Order(7)
    void testDuplicateEmailThrowsException() {
        userService.createUser(new CreateUserDto("Tony Stark", "tony@example.com", "password"));

        var exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(new CreateUserDto("Tony Stark", "tony@example.com", "password"));
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @Order(8)
    void testListUsers() {
        userService.createUser(new CreateUserDto("User One", "one@example.com", "password"));
        userService.createUser(new CreateUserDto("User Two", "two@example.com", "password"));

        var users = userService.list();

        assertEquals(2, users.size());
    }
}
