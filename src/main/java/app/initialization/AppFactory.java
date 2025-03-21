package app.initialization;

import app.auth.Authenticator;
import app.config.AppProperties;
import app.config.AuthenticationConfig;
import app.config.DbConfig;
import app.config.LiquibaseConfig;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.mapper.UserMapper;
import app.repository.FinanceRepository;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.TransactionJdbcRepository;
import app.repository.jdbc.UserJdbcRepository;
import app.service.AuthService;
import app.service.FinanceService;
import app.service.TransactionService;
import app.service.UserService;
import app.service.impl.*;
import app.util.ConfigLoader;
import app.util.in.UserAuth;
import app.util.in.UserInput;
import app.util.out.Menu;
import app.util.out.UserOutput;

import java.util.HashMap;
import java.util.Scanner;

public class AppFactory {

    private final DbConfig dbConfig;
    private final AuthenticationConfig authenticationConfig;
    private final AppProperties appProperties;

    public AppFactory() {
        this.appProperties = ConfigLoader.loadConfig("application.yml", AppProperties.class);
        this.dbConfig = new DbConfig(appProperties.getDb());
        this.authenticationConfig = new AuthenticationConfig(new HashMap<>());
    }

    public static App initialization(String... args) {
        AppFactory appFactory = new AppFactory();

        appFactory.initializeLiquibase();

        UserService userService = appFactory.createUserService();
        AuthService authService = appFactory.createAuthService(userService);

        TransactionService transactionService = appFactory.createTransactionService();

        FinanceService financeService = appFactory.createFinanceService(userService, transactionService);

        UserAuth userAuth = new UserAuth(userService, authService, appFactory.createUserOutput(), appFactory.createUserInput());

        Menu menu = appFactory.createMenu(userAuth, userService, financeService);

        menu.showMenu();

        return new App(userService, authService, financeService, transactionService, menu);
    }


    public UserInput createUserInput() {
        return new UserInput(new Scanner(System.in));
    }

    public UserOutput createUserOutput() {
        return new UserOutput();
    }

    public Authenticator createAuthenticator() {
        return new Authenticator(authenticationConfig);
    }

    public FinanceRepository createFinanceRepository() {
        return new FinanceJdbcRepository(dbConfig.getConnection());
    }

    public UserService createUserService() {
        FinanceRepository financeRepository = createFinanceRepository();
        return new UserServiceImpl(new UserMapper(), new UserJdbcRepository(dbConfig.getConnection()), financeRepository, new FinanceMapper());
    }

    public AuthService createAuthService(UserService userService) {
        return new AuthServiceImpl(authenticationConfig, createAuthenticator(), userService);
    }

    public TransactionService createTransactionService() {
        return new TransactionServiceImpl(new TransactionJdbcRepository(dbConfig.getConnection()), new TransactionMapper());
    }

    public FinanceService createFinanceService(UserService userService, TransactionService transactionService) {
        FinanceRepository financeRepository = createFinanceRepository();
        return new FinanceServiceImpl(financeRepository, userService, transactionService, new FinanceMapper(), new TransactionMapper(), new NotificationServiceImpl());
    }

    public Menu createMenu(UserAuth userAuth, UserService userService, FinanceService financeService) {
        UserInput userInput = createUserInput();
        UserOutput userOutput = createUserOutput();
        return new Menu(userOutput, userInput, financeService, userAuth, userService, new TargetServiceImpl(userService, financeService));
    }

    public void initializeLiquibase() {
        LiquibaseConfig liquibaseConfig = new LiquibaseConfig(dbConfig, appProperties.getLiquibase());
        liquibaseConfig.connect();
    }

}

