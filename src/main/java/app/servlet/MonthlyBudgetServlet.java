package app.servlet;

import app.aspect.exception.CustomExceptionHandler;
import app.service.TargetService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@WebServlet("/api/v1/target/budget")
@CustomExceptionHandler
public class MonthlyBudgetServlet extends BaseServlet {
    private TargetService targetService;
    private JsonMapper jsonMapper;

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        this.targetService = app.getTargetService();
        this.jsonMapper = app.getJsonMapper();
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
