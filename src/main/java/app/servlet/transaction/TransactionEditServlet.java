package app.servlet.transaction;

import app.aspect.exception.CustomExceptionHandler;
import app.aspect.validator.ValidateDto;
import app.container.Component;
import app.dto.transaction.TransactionDto;
import app.dto.transaction.UpdateTransactionDto;
import app.service.TransactionService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@CustomExceptionHandler
public class TransactionEditServlet extends BaseServlet {

    private JsonMapper jsonMapper;
    private TransactionService transactionService;

    public TransactionEditServlet(JsonMapper jsonMapper, TransactionService transactionService) {
        this.jsonMapper = jsonMapper;
        this.transactionService = transactionService;
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UpdateTransactionDto updateTransactionDto = jsonMapper.readValue(req.getInputStream(), UpdateTransactionDto.class);

        handleTransactionEdit(updateTransactionDto, resp);
    }

    private void handleTransactionEdit(@ValidateDto UpdateTransactionDto updateTransactionDto, HttpServletResponse resp) throws IOException {
        TransactionDto dto = transactionService.edit(updateTransactionDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write(jsonMapper.writeValueAsString(dto));
    }
}
