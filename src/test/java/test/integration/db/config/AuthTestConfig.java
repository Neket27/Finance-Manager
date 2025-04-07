//package test.integration.db.config;
//
//import app.repository.jdbc.UserJdbcRepository;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import test.integration.db.TestDatabase;
//import test.integration.db.TestDatabaseFactory;
//
//@TestConfiguration
//public class AuthTestConfig {
//
//    @Bean
//    UserJdbcRepository userJdbcRepository() {
//        TestDatabase database = TestDatabaseFactory.create();
//        return new UserJdbcRepository(database.getJdbcTemplate);
//    }
//
//}
