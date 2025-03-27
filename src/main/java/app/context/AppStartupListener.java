package app.context;

import app.auth.AuthenticationFilter;
import app.config.AppProperties;
import app.container.SimpleIoCContainer;
import app.exception.IocException;
import app.servlet.*;
import app.servlet.auth.ServletLogin;
import app.servlet.auth.ServletLogout;
import app.servlet.auth.ServletRegister;
import app.servlet.transaction.*;
import app.util.ConfigLoader;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        SimpleIoCContainer container = new SimpleIoCContainer();

        try {
            container.autoRegister("app");

            AppProperties appProperties = ConfigLoader.loadConfig("application.yml", AppProperties.class);
            System.out.println("Загруженные AppProperties: " + appProperties);
            System.out.println("LiquibaseProperties: " + appProperties.getLiquibase());
            System.out.println("Liquibase changeLog: " + (appProperties.getLiquibase() != null ? appProperties.getLiquibase().getChangeLogFile() : "NULL"));


            AuthenticationFilter filter = container.getInstance(AuthenticationFilter.class);
            sce.getServletContext()
                    .addFilter("AuthenticationFilter", filter)
                    .addMappingForUrlPatterns(null, false, "/*");

            ServletLogin loginServlet = container.getInstance(ServletLogin.class);
            sce.getServletContext()
                    .addServlet("ServletLogin", loginServlet)
                    .addMapping("/api/v1/auth/signin");

            ServletRegister servletRegister = container.getInstance(ServletRegister.class);
            sce.getServletContext()
                    .addServlet("ServletRegister", servletRegister)
                    .addMapping("/api/v1/auth/signup");

            ServletLogout servletLogout = container.getInstance(ServletLogout.class);
            sce.getServletContext()
                    .addServlet("ServletLogout", servletLogout)
                    .addMapping("/api/v1/auth/logout");

            TransactionCreateServlet transactionCreateServlet = container.getInstance(TransactionCreateServlet.class);
            sce.getServletContext()
                    .addServlet("TransactionCreateServlet", transactionCreateServlet)
                    .addMapping("/api/v1/transaction");

            TransactionDeleteServlet transactionDeleteServlet = container.getInstance(TransactionDeleteServlet.class);
            sce.getServletContext()
                    .addServlet("TransactionDeleteServlet", transactionDeleteServlet)
                    .addMapping("/api/v1/transaction/delete");

            TransactionEditServlet transactionEditServlet = container.getInstance(TransactionEditServlet.class);
            sce.getServletContext()
                    .addServlet("TransactionEditServlet", transactionEditServlet)
                    .addMapping("/api/v1/transaction/edit");

            TransactionFilterServlet transactionFilterServlet = container.getInstance(TransactionFilterServlet.class);
            sce.getServletContext()
                    .addServlet("TransactionFilterServlet", transactionFilterServlet)
                    .addMapping("/api/v1/transaction/filter");

            TransactionGetListServlet transactionGetListServlet = container.getInstance(TransactionGetListServlet.class);
            sce.getServletContext()
                    .addServlet("TransactionGetListServlet", transactionGetListServlet)
                    .addMapping("/api/v1/transaction/getlist");

            BlockUserServlet blockUserServlet = container.getInstance(BlockUserServlet.class);
            sce.getServletContext()
                    .addServlet("BlockUserServlet", blockUserServlet)
                    .addMapping("/api/v1/block/user");

            DeleteUserServlet deleteUserServlet = container.getInstance(DeleteUserServlet.class);
            sce.getServletContext()
                    .addServlet("DeleteUserServlet", deleteUserServlet)
                    .addMapping("/api/v1/delete/user");

            FinancialReportServlet financialReportServlet = container.getInstance(FinancialReportServlet.class);
            sce.getServletContext()
                    .addServlet("FinancialReportServlet", financialReportServlet)
                    .addMapping("/api/v1/financial/report");

            MonthlyBudgetServlet monthlyBudgetServlet = container.getInstance(MonthlyBudgetServlet.class);
            sce.getServletContext()
                    .addServlet("MonthlyBudgetServlet", monthlyBudgetServlet)
                    .addMapping("/api/v1/monthlybudget");

            ProgressServlet progressServlet = container.getInstance(ProgressServlet.class);
            sce.getServletContext()
                    .addServlet("ProgressServlet", progressServlet)
                    .addMapping("/api/v1/progress");

            SavingsGoalServlet savingsGoalServlet = container.getInstance(SavingsGoalServlet.class);
            sce.getServletContext()
                    .addServlet("SavingsGoalServlet", savingsGoalServlet)
                    .addMapping("/api/v1/savings/goal");

        } catch (Exception e) {
            throw new IocException(e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Приложение остановлено.");
    }
}
