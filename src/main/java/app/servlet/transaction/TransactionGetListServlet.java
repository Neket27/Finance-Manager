package app.servlet.transaction;

import app.context.UserContext;
import app.dto.transaction.TransactionDto;
import app.dto.user.UserDto;
import app.aspect.exception.CustomExceptionHandler;
import app.service.TransactionService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/transactions")
@CustomExceptionHandler
public class TransactionGetListServlet extends BaseServlet {

    private JsonMapper jsonMapper;
    private TransactionService transactionService;

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        this.transactionService = app.getTransactionService();
        this.jsonMapper = app.getJsonMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto user = UserContext.getCurrentUser();
        List<TransactionDto> transactionDtos =transactionService.getTransactionsByFinanceId(user.financeId());

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getOutputStream().write(jsonMapper.writeValueAsBytes(transactionDtos));
    }
}
