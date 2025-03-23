package app.initialization;

import app.service.AuthService;
import app.service.FinanceService;
import app.service.TransactionService;
import app.service.UserService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class App {

    private final JsonMapper jsonMapper;
    private final UserService userService;
    private final AuthService authService;
    private final FinanceService financeService;
    private final TransactionService transactionService;

}
