package app.servlet.transaction;

import app.aspect.exception.CustomExceptionHandler;
import app.container.Component;
import app.context.UserContext;
import app.dto.transaction.TransactionDto;
import app.dto.user.UserDto;
import app.service.TransactionService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Component
@CustomExceptionHandler
public class TransactionGetListServlet extends BaseServlet {

    private JsonMapper jsonMapper;
    private TransactionService transactionService;

    public TransactionGetListServlet(JsonMapper jsonMapper, TransactionService transactionService) {
        this.jsonMapper = jsonMapper;
        this.transactionService = transactionService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto user = UserContext.getCurrentUser();
        List<TransactionDto> transactionDtos = transactionService.getTransactionsByFinanceId(user.financeId());

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getOutputStream().write(jsonMapper.writeValueAsBytes(transactionDtos));
    }
}
