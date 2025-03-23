package app.servlet.auth;

import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.service.AuthService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/v1/auth/signup")
public class ServletRegister extends BaseServlet {

    private AuthService authService;
    private JsonMapper mapper;

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        this.authService = app.getAuthService();
        this.mapper = app.getJsonMapper();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CreateUserDto createUserDto = mapper.readValue(req.getInputStream(), CreateUserDto.class);
        UserDto userDto = authService.register(createUserDto);


        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(mapper.writeValueAsString(userDto));
    }

}
