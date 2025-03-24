package app.servlet.transaction;

import app.aspect.exception.CustomExceptionHandler;
import app.container.Component;
import app.service.TransactionService;
import app.servlet.BaseServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@CustomExceptionHandler
public class TransactionDeleteServlet extends BaseServlet {

    private TransactionService transactionService;

    public TransactionDeleteServlet(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String idParam = req.getParameter("id");
        transactionService.delete(Long.parseLong(idParam));

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
