package app.servlet;

import app.aspect.exception.CustomExceptionHandler;
import app.container.Component;
import app.service.TargetService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@Component
@CustomExceptionHandler
public class FinancialReportServlet extends BaseServlet {

    private  TargetService targetService;
    private  JsonMapper jsonMapper;

    public FinancialReportServlet(TargetService targetService, JsonMapper jsonMapper) {
        this.targetService = targetService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String report = targetService.generateFinancialReport();

        resp.setContentType("application/json");
        resp.getWriter().write(jsonMapper.writeValueAsString(Map.of("report", report)));
    }
}
