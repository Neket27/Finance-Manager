package test.controller;

import app.controller.AuthController;
import app.controller.advice.AuthExceptionHandler;
import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.entity.Role;
import app.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(AuthExceptionHandler.class)
public class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthController authController;

    @Mock
    private AuthService authServiceMock;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        this.authController = new AuthController(authServiceMock);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    private final UserDto userDto = new UserDto(1L, "name", "email@mail.com", "1234QWAS", true, Role.USER, 1L);

    @Test
    void registerSuccess() throws Exception {
        CreateUserDto createUserDto = new CreateUserDto(userDto.name(), userDto.email(), userDto.password());

        when(authServiceMock.register(createUserDto)).thenReturn(userDto);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(createUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.id()))
                .andExpect(jsonPath("$.name").value(userDto.name()))
                .andExpect(jsonPath("$.email").value(userDto.email()))
                .andExpect(jsonPath("$.isActive").value(userDto.isActive()))
                .andExpect(jsonPath("$.role").value(userDto.role().toString()));
    }

    @Test
    void registerFail() throws Exception {
        CreateUserDto createUserDto = new CreateUserDto(userDto.name(), "", userDto.password());

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(createUserDto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void loginSuccess() throws Exception {
        SignIn signIn = new SignIn(userDto.email(), userDto.email());
        ResponseLogin responseLogin = new ResponseLogin("1");

        when(authServiceMock.login(signIn)).thenReturn(responseLogin);

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(signIn)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(responseLogin.token()));
    }

    @Test
    void loginFail() throws Exception {
        SignIn signIn = new SignIn(userDto.email(), "1234");

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(signIn)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void logoutSuccess() throws Exception {

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(1)))
                .andExpect(status().isOk());
    }

//    @Test
//    void logoutFail() throws Exception {
//
//        doThrow(new ErrorLogoutException("Ошибка выхода")).when(authServiceMock).logout();
//
//        mockMvc.perform(post("/api/v1/auth/logout")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .characterEncoding("UTF-8")
//                        .content(asJsonString(1)))
//                .andDo(print())
//                .andExpect(status().isUnauthorized());
//    }


    private String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
