package app.servlet.transaction;

import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.aspect.exception.CustomExceptionHandler;
import app.service.TransactionService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/v1/transaction/edit")
@CustomExceptionHandler
public class TransactionEditServlet extends BaseServlet {

    private JsonMapper jsonMapper;
    private TransactionService transactionService;

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        this.transactionService = app.getTransactionService();
        this.jsonMapper = app.getJsonMapper();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TransactionDto dto = transactionService.edit(jsonMapper.readValue(req.getInputStream(), UpdateTransactionDto.class));

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write(jsonMapper.writeValueAsString(dto));
    }
}
