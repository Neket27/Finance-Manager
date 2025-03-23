package app.servlet.transaction;

import app.aspect.exception.CustomExceptionHandler;
import app.service.TransactionService;
import app.servlet.BaseServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/v1/transaction/delete")
@CustomExceptionHandler
public class TransactionDeleteServlet extends BaseServlet {

    private TransactionService transactionService;

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        this.transactionService = app.getTransactionService();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String idParam = req.getParameter("id");
        transactionService.delete(Long.parseLong(idParam));

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
