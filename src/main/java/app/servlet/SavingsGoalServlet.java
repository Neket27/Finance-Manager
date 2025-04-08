package app.servlet;

import app.aspect.exception.CustomExceptionHandler;
import app.container.Component;
import app.service.TargetService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Component
@CustomExceptionHandler
public class SavingsGoalServlet extends BaseServlet {

    private TargetService targetService;
    private JsonMapper jsonMapper;

    public SavingsGoalServlet(TargetService targetService, JsonMapper jsonMapper) {
        this.targetService = targetService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BigDecimal savingGoal = new BigDecimal(req.getParameter("goal"));
        targetService.updateGoalSavings(savingGoal);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(jsonMapper.writeValueAsString(Map.of("message", "Цель накопления установлена!")));
    }
}
