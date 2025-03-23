package app.servlet.auth;

import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.aspect.exception.CustomExceptionHandler;
import app.service.AuthService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/v1/auth/signin")
@CustomExceptionHandler
public class ServletLogin extends BaseServlet {

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
        SignIn signin = mapper.readValue(req.getInputStream(), SignIn.class);
        ResponseLogin responseLogin = authService.login(signin);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write(mapper.writeValueAsString(responseLogin));
    }

}

