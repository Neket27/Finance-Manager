package app.initialization;

import app.auth.Authenticator;
import app.config.AppProperties;
import app.config.DbConfig;
import app.config.LiquibaseConfig;
import app.mapper.FinanceMapper;
import app.mapper.TransactionMapper;
import app.mapper.UserMapper;
import app.repository.FinanceRepository;
import app.repository.jdbc.FinanceJdbcRepository;
import app.repository.jdbc.TokenJdbcRepository;
import app.repository.jdbc.TransactionJdbcRepository;
import app.repository.jdbc.UserJdbcRepository;
import app.service.*;
import app.service.impl.*;
import app.util.ConfigLoader;
import app.util.in.UserInput;
import app.util.out.UserOutput;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Scanner;

public class AppFactory {

    private final DbConfig dbConfig;
    private final AppProperties appProperties;

    public AppFactory() {
        this.appProperties = ConfigLoader.loadConfig("application.yml", AppProperties.class);
        this.dbConfig = new DbConfig(appProperties.getDb());
    }

    public static App initialization(String... args) {
        AppFactory appFactory = new AppFactory();

        appFactory.initializeLiquibase();

        JsonMapper jsonMapper = appFactory.createMapper();

        UserService userService = appFactory.createUserService();

        TokenService tokenService = appFactory.createTokenService();

        Authenticator authenticator = appFactory.createAuthenticator(tokenService, userService);

        AuthService authService = appFactory.createAuthService(userService, authenticator, tokenService);

        TransactionService transactionService = appFactory.createTransactionService();

        FinanceRepository financeRepository = appFactory.createFinanceRepository();

        FinanceService financeService = appFactory.createFinanceService(userService, transactionService, financeRepository);

        TargetService targetService = appFactory.createTargetService(userService, financeService);

        return new App(jsonMapper, userService, authService, financeService, transactionService, authenticator,targetService);
    }

    public JsonMapper createMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    public UserInput createUserInput() {
        return new UserInput(new Scanner(System.in));
    }

    public UserOutput createUserOutput() {
        return new UserOutput();
    }

    public Authenticator createAuthenticator(TokenService tokenService, UserService userService) {
        return new Authenticator(tokenService, userService);
    }

    public FinanceRepository createFinanceRepository() {
        return new FinanceJdbcRepository(dbConfig.getConnection());
    }

    public UserService createUserService() {
        FinanceRepository financeRepository = createFinanceRepository();
        return new UserServiceImpl(new UserMapper(), new UserJdbcRepository(dbConfig.getConnection()), financeRepository, new FinanceMapper());
    }

    public AuthService createAuthService(UserService userService, Authenticator authenticator, TokenService tokenService) {
        return new AuthServiceImpl(userService, tokenService);
    }

    public TransactionService createTransactionService() {
        return new TransactionServiceImpl(new TransactionJdbcRepository(dbConfig.getConnection()), new TransactionMapper());
    }

    public FinanceService createFinanceService(UserService userService, TransactionService transactionService, FinanceRepository financeRepository) {
        return new FinanceServiceImpl(financeRepository, userService, transactionService, new FinanceMapper(), new TransactionMapper(), new NotificationServiceImpl());
    }

    public TokenJdbcRepository createTokenRepository() {
        return new TokenJdbcRepository(dbConfig.getConnection());
    }

    public TokenService createTokenService() {
        return new TokenServiceImpl(createTokenRepository());
    }

    private TargetService createTargetService(UserService userService, FinanceService financeService) {
        return new TargetServiceImpl(userService, financeService);
    }

    public void initializeLiquibase() {
        LiquibaseConfig liquibaseConfig = new LiquibaseConfig(dbConfig, appProperties.getLiquibase());
        liquibaseConfig.connect();
    }

}

