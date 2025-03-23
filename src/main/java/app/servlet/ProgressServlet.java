package app.servlet;

import app.context.UserContext;
import app.aspect.exception.CustomExceptionHandler;
import app.service.FinanceService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/v1/target/progress")
@CustomExceptionHandler
public class ProgressServlet extends BaseServlet {

    private FinanceService financeService;

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        this.financeService = app.getFinanceService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Double progress = financeService.getProgressTowardsGoal(UserContext.getCurrentUser().email());
        resp.getWriter().write("Прогресс накопления: " + progress);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
