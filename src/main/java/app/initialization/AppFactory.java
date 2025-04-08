//package app.initialization;
//
//import app.auth.Authenticator;
//import app.config.AppProperties;
//import app.config.DbConfig;
//import app.config.LiquibaseConfig;
//import app.mapper.FinanceMapper;
//import app.mapper.TransactionMapper;
//import app.mapper.UserMapper;
//import app.repository.FinanceRepository;
//import app.repository.jdbc.FinanceJdbcRepository;
//import app.repository.jdbc.TokenJdbcRepository;
//import app.repository.jdbc.TransactionJdbcRepository;
//import app.repository.jdbc.UserJdbcRepository;
//import app.service.*;
//import app.service.impl.*;
//import app.util.ConfigLoader;
//import app.util.in.UserInput;
//import app.util.out.UserOutput;
//import com.fasterxml.jackson.databind.json.JsonMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//
//import java.sql.Connection;
//import java.util.Scanner;
//
//public class AppFactory {
//
//    private final DbConfig dbConfig;
//    private final AppProperties appProperties;
//    private final Connection connection;
//
//    private final UserMapper userMapper;
//    private final FinanceMapper financeMapper;
//    private final TransactionMapper transactionMapper;
//
//    public AppFactory() {
//        this.appProperties = ConfigLoader.loadConfig("application.yml", AppProperties.class);
//        this.dbConfig = new DbConfig(appProperties.getDb());
//        this.connection = dbConfig.getConnection();
//        this.userMapper = new UserMapper();
//        this.financeMapper = new FinanceMapper();
//        this.transactionMapper = new TransactionMapper();
//    }
//
//    public static App initialization(String... args) {
//        AppFactory factory = new AppFactory();
//        factory.initializeLiquibase();
//
//        JsonMapper jsonMapper = factory.createJsonMapper();
//        NotificationService notificationService = factory.createNotificationService();
//
//        // DAO / Repository слои
//        UserJdbcRepository userRepo = new UserJdbcRepository(factory.connection);
//        FinanceRepository financeRepo = new FinanceJdbcRepository(factory.connection);
//        TransactionJdbcRepository transactionRepo = new TransactionJdbcRepository(factory.connection);
//        TokenJdbcRepository tokenRepo = new TokenJdbcRepository(factory.connection);
//
//        // Сервисы
//        TransactionService transactionService = new TransactionServiceImpl(transactionRepo, factory.transactionMapper);
//        FinanceService financeService = new FinanceServiceImpl(financeRepo, transactionService, factory.financeMapper);
//        UserService userService = new UserServiceImpl(factory.userMapper, userRepo, financeService, factory.financeMapper);
//        TokenService tokenService = new TokenServiceImpl(tokenRepo);
//        Authenticator authenticator = new Authenticator(tokenService, userService);
//        AuthService authService = new AuthServiceImpl(userService, tokenService);
//        TargetService targetService = new TargetServiceImpl(userService, financeService);
//
//        return new App(jsonMapper, userService, authService, financeService, transactionService, authenticator, targetService);
//    }
//
//    private JsonMapper createJsonMapper() {
//        return JsonMapper.builder()
//                .addModule(new JavaTimeModule())
//                .build();
//    }
//
//    private NotificationService createNotificationService() {
//        return new NotificationServiceImpl();
//    }
//
//    public UserInput createUserInput() {
//        return new UserInput(new Scanner(System.in));
//    }
//
//    public UserOutput createUserOutput() {
//        return new UserOutput();
//    }
//
//    private void initializeLiquibase() {
//        LiquibaseConfig liquibaseConfig = new LiquibaseConfig(dbConfig, appProperties.getLiquibase());
//        liquibaseConfig.connect();
//    }
//}
