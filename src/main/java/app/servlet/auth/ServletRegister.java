package app.servlet.auth;

import app.aspect.exception.CustomExceptionHandler;
import app.container.Component;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.service.AuthService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@CustomExceptionHandler
public class ServletRegister extends BaseServlet {

    private JsonMapper mapper;
    public AuthService authService;

    public ServletRegister(JsonMapper mapper, AuthService authService) {
        this.mapper = mapper;
        this.authService = authService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CreateUserDto createUserDto = mapper.readValue(req.getInputStream(), CreateUserDto.class);
        UserDto userDto = authService.register(createUserDto);
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.setContentType("application/json");
        resp.getWriter().write(mapper.writeValueAsString(userDto));
    }
}
