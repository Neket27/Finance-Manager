package app.servlet.auth;

import app.aspect.exception.CustomExceptionHandler;
import app.container.Component;
import app.service.AuthService;
import app.servlet.BaseServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@CustomExceptionHandler
public class ServletLogout extends BaseServlet {

    private AuthService authService;

    public ServletLogout(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        authService.logout();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\": \"Вы успешно вышли из системы\"}");
    }
}
