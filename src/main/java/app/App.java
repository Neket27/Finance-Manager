package app;

import app.auth.Authenticator;
import app.config.AuthenticationConfig;
import app.config.LiquibaseConfig;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.mapper.UserMapper;
import app.repository.FinanceRepository;
import app.repository.bd.FinanceJdbcRepository;
import app.repository.bd.TransactionJdbcRepository;
import app.repository.bd.UserJdbcRepository;
import app.service.AuthService;
import app.service.FinanceService;
import app.service.TransactionService;
import app.service.UserService;
import app.service.impl.*;
import app.util.in.UserAuth;
import app.util.in.UserInput;
import app.util.out.Menu;
import app.util.out.UserOutput;

import java.util.HashMap;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        AuthenticationConfig authenticationConfig = new AuthenticationConfig(new HashMap<>());
        Authenticator authenticator = new Authenticator(authenticationConfig);
        FinanceRepository financeRepository = new FinanceJdbcRepository();
        FinanceMapper financeMapper = new FinanceMapper();
        TransactionMapper transactionMapper = new TransactionMapper();
        UserService userService = new UserServiceImpl(new UserMapper(), new UserJdbcRepository(), financeRepository, financeMapper);
        AuthService authService = new AuthServiceImpl(authenticationConfig, authenticator, userService);
        TransactionService transactionService = new TransactionServiceImpl(new TransactionJdbcRepository(), transactionMapper);
        FinanceService financeService = new FinanceServiceImpl(financeRepository, userService, transactionService, financeMapper, transactionMapper, new NotificationServiceImpl());

        UserInput userInput = new UserInput(new Scanner(System.in));
        UserOutput userOutput = new UserOutput();
        UserAuth userAuth = new UserAuth(userService, authService, userOutput, userInput);
        Menu menu = new Menu(userOutput, userInput, financeService, userAuth, userService, new TargetServiceImpl(userService, financeService));

        LiquibaseConfig.connect();
        menu.showMenu();

    }

}
