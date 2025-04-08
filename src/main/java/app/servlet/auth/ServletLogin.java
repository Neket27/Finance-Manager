package app.servlet.auth;

import app.aspect.exception.CustomExceptionHandler;
import app.aspect.validator.ValidateDto;
import app.container.Component;
import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.service.AuthService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@CustomExceptionHandler
public class ServletLogin extends BaseServlet {

    private AuthService authService;
    private JsonMapper mapper;

    public ServletLogin(AuthService authService, JsonMapper mapper) {
        this.authService = authService;
        this.mapper = mapper;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SignIn signIn = mapper.readValue(req.getInputStream(), SignIn.class);
        handleLogin(signIn, resp);
    }

    private void handleLogin(@ValidateDto SignIn signIn, HttpServletResponse resp) throws IOException {
        ResponseLogin responseLogin = authService.login(signIn);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write(mapper.writeValueAsString(responseLogin));
    }
}
