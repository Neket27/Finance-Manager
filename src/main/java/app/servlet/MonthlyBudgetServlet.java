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
public class MonthlyBudgetServlet extends BaseServlet {

    private TargetService targetService;
    private JsonMapper jsonMapper;

    public MonthlyBudgetServlet(TargetService targetService, JsonMapper jsonMapper) {
        this.targetService = targetService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var node = jsonMapper.readTree(req.getInputStream());
        BigDecimal monthlyBudget = new BigDecimal(node.get("budget").asText());

        targetService.setMonthlyBudget(monthlyBudget);

        resp.setContentType("application/json");
        resp.getWriter().write(jsonMapper.writeValueAsString(Map.of("message", "Месячный бюджет установлен!")));
    }
}
