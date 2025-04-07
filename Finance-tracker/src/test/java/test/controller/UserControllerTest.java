package test.controller;

import app.controller.UserController;
import app.entity.Role;
import app.entity.User;
import app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import neket27.context.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    private UserContext userContext;
    private ObjectMapper objectMapper;
    private UserController userController;

    @Mock
    private UserService userService;


    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.userController = new UserController(userService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        this.userContext = new UserContext();

        this.userContext.setUser(new User(1L, "name", "email@mail.ru", "password", true, Role.ADMIN, 1L));
    }

    @Test
    void blockUserTest() throws Exception {
        String email = "test@example.com";

        when(userService.blockUser(email)).thenReturn(true);

        mockMvc.perform(post("/api/v1/user/block")
                        .param("email", email))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).blockUser(email);
    }


    @Test
    void blockUserFiledTest() throws Exception {
        String email = "test@example.com";

        UserContext userContext = new UserContext();
        userContext.setUser(new User(1L, "name", "email@mail.ru", "password", true, Role.USER, 1L));

        mockMvc.perform(post("/api/v1/user/block")
                        .param("email", email))
                .andExpect(status().isForbidden());

    }


    @Test
    void deleteUserTest() throws Exception {
        String email = "test@example.com";

        when(userService.remove(email)).thenReturn(true);

        mockMvc.perform(post("/api/v1/user/delete")
                        .param("email", email))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).remove(email);
    }

    @Test
    void deleteUserFiledTest() throws Exception {
        String email = "test@example.com";

        UserContext userContext = new UserContext();
        userContext.setUser(new User(1L, "name", "email@mail.ru", "password", true, Role.USER, 1L));

        mockMvc.perform(post("/api/v1/user/delete")
                        .param("email", email))
                .andExpect(status().isForbidden());
    }
}
