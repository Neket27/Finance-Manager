package app.initialization;

import app.auth.Authenticator;
import app.service.*;
import com.fasterxml.jackson.databind.json.JsonMapper;


public class App {

    private final JsonMapper jsonMapper;
    private final UserService userService;
    private final AuthService authService;
    private final FinanceService financeService;
    private final TransactionService transactionService;
    private final Authenticator authenticator;
    private final TargetService targetService;

    public App(JsonMapper jsonMapper, UserService userService, AuthService authService, FinanceService financeService, TransactionService transactionService, Authenticator authenticator, TargetService targetService) {
        this.jsonMapper = jsonMapper;
        this.userService = userService;
        this.authService = authService;
        this.financeService = financeService;
        this.transactionService = transactionService;
        this.authenticator = authenticator;
        this.targetService = targetService;
    }

    public TargetService getTargetService() {
        return targetService;
    }

    public JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    public UserService getUserService() {
        return userService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public FinanceService getFinanceService() {
        return financeService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }
}
