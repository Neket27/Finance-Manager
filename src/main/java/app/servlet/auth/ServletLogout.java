package app.servlet.auth;

import app.service.AuthService;
import app.servlet.BaseServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/api/v1/auth/logout")
public class ServletLogout extends BaseServlet {

    private AuthService authService;

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        authService = app.getAuthService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        authService.logout();

        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
