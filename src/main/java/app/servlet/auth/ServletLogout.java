package app.servlet.auth;

import app.service.AuthService;
import app.servlet.BaseServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/v1/auth/logout")
public class ServletLogout extends BaseServlet {

    private AuthService authService;

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        authService = app.getAuthService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        authService.logout();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\": \"Вы успешно вышли из системы\"}");
    }
}
