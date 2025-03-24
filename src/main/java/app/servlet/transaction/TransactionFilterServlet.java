package app.servlet.transaction;

import app.aspect.validator.ValidateDto;
import app.container.Component;
import app.dto.transaction.FilterTransactionDto;
import app.dto.transaction.TransactionDto;
import app.service.TransactionService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Component
public class TransactionFilterServlet extends BaseServlet {

    private JsonMapper jsonMapper;
    private TransactionService transactionService;

    public TransactionFilterServlet(JsonMapper jsonMapper, TransactionService transactionService) {
        this.jsonMapper = jsonMapper;
        this.transactionService = transactionService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        FilterTransactionDto filterTransactionDto = jsonMapper.readValue(req.getInputStream(), FilterTransactionDto.class);
        handleTransactionFilter(filterTransactionDto, resp);
    }

    private void handleTransactionFilter(@ValidateDto FilterTransactionDto filterTransactionDto, HttpServletResponse resp) throws IOException {
        List<TransactionDto> transactionDtos = transactionService.getFilteredTransactions(filterTransactionDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getOutputStream().write(jsonMapper.writeValueAsBytes(transactionDtos));
    }
}
