package app.servlet;

import app.initialization.App;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServlet;

public abstract class BaseServlet extends HttpServlet {

    public App app;

    @Override
    public void init(ServletConfig config) {
        this.app = (App) config.getServletContext().getAttribute(App.class.getName());
//        this.app = (app) config.getServletContext().getAttribute("app");
    }
}
