package test.integration;

import app.App;
import app.dto.user.CreateUserDto;
import app.entity.Role;
import app.entity.User;
import app.mapper.UserMapper;
import app.service.UserService;
import neket27.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.integration.db.config.LiquibaseConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = App.class)
@ActiveProfiles("test")
@Testcontainers
@Import({LiquibaseConfig.class})
public class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LiquibaseConfig liquibaseConfig;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.sql");

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
    }

    @AfterEach
    void tearDown() {
        liquibaseConfig.dropSchemas();
        liquibaseConfig.initialize();
        UserContext.clear();
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
        CreateUserDto createUserDto = new CreateUserDto("Dummy", "dummy@example.com", "pass");
        User entity = userMapper.toEntity(createUserDto);
        userService.createUser(entity);

        // Act
        var ex = assertThrows(IllegalArgumentException.class, () -> userService.updateDataUser(null, "dummy@example.com"));

        // Assert
        assertEquals("User Dto не может быть null", ex.getMessage());
    }

    @Test
    void testUpdateNonExistingUser_ShouldThrowNotFoundException() {
        // Arrange
        User update = User.builder()
                .name("Ghost")
                .email("ghost@example.com")
                .password("pass")
                .build();

        // Act
        var ex = assertThrows(Exception.class, () -> userService.updateDataUser(update, "ghost@example.com"));

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
        CreateUserDto createUserDto = new CreateUserDto("Finance Check", "finance@example.com", "pass");
        User entity = userMapper.toEntity(createUserDto);
        User returnUser = userService.createUser(entity);

        // Assert
        assertNotNull(returnUser);
    }

    @Test
    void testChangeUserRole_ShouldSetUserContext() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("Clark Kent", "superman@example.com", "kryptonite");
        User entity = userMapper.toEntity(createUserDto);
        userService.createUser(entity);

        // Act
        boolean changed = userService.changeUserRole("superman@example.com", Role.ADMIN);

        // Assert
        assertTrue(changed);
        assertNotNull(UserContext.getCurrentUser());
        User user = (User) UserContext.getCurrentUser();
        assertEquals("superman@example.com", user.getEmail());
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
        CreateUserDto createUserDto = new CreateUserDto("Temp User", "temp@example.com", "pass");
        User entity = userMapper.toEntity(createUserDto);
        userService.createUser(entity);
        userService.remove("temp@example.com");

        // Act
        List<?> users = userService.list();

        // Assert
        assertEquals(0, users.size());
    }

    @Test
    void testCreateMultipleAdmins_NotPossible() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("Admin1", "admin1@example.com", "pass");
        User entity = userMapper.toEntity(createUserDto);
        userService.createUser(entity);

        // Act
        CreateUserDto createUserDto2 = new CreateUserDto("Admin2", "admin2@example.com", "pass");
        User entity2 = userMapper.toEntity(createUserDto2);
        var user = userService.createUser(entity2);

        // Assert
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void testRemoveUser_ShouldReturnTrueIfUserRemoved() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("Jane Doe", "jane@example.com", "pass");
        User entity = userMapper.toEntity(createUserDto);
        userService.createUser(entity);

        // Act
        boolean removed = userService.remove("jane@example.com");

        // Assert
        assertTrue(removed);
    }

    @Test
    void testBlockUser_ShouldReturnTrueIfUserBlocked() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("Blocked User", "blocked@example.com", "pass");
        User entity = userMapper.toEntity(new CreateUserDto("Blocked User", "blocked@example.com", "pass"));
        userService.createUser(entity);

        // Act
        boolean result = userService.blockUser("blocked@example.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void testListUsers_ShouldReturnCorrectUsers() {
        // Arrange
        CreateUserDto createUserDto1 = new CreateUserDto("User 1", "user1@example.com", "pass");
        CreateUserDto createUserDto2 = new CreateUserDto("User 2", "user2@example.com", "pass");
        User entity1 = userMapper.toEntity(createUserDto1);
        User entity2 = userMapper.toEntity(createUserDto2);
        userService.createUser(entity1);
        userService.createUser(entity2);

        // Act
        List<?> users = userService.list();

        // Assert
        assertEquals(2, users.size());
    }

}
