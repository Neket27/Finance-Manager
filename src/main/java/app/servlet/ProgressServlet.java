package app.servlet;

import app.aspect.exception.CustomExceptionHandler;
import app.container.Component;
import app.context.UserContext;
import app.service.FinanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@CustomExceptionHandler
public class ProgressServlet extends BaseServlet {

    private FinanceService financeService;

    public ProgressServlet(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Double progress = financeService.getProgressTowardsGoal(UserContext.getCurrentUser().id());
        resp.getWriter().write("Прогресс накопления: " + progress);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
