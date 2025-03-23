package app.context;//package app.t.servlet;

import app.initialization.App;
import app.initialization.AppFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Приложение стартовало!");

        runApp(sce);
    }

    private void runApp(ServletContextEvent sce) {
        App app = AppFactory.initialization();
        sce.getServletContext().setAttribute(App.class.getName(), app);
        System.out.println("Выполняем начальные операции...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Приложение остановлено.");
    }
}
