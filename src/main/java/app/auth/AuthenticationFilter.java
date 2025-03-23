package app.auth;

import app.context.UserContext;
import app.dto.user.UserDto;
import app.initialization.App;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private Authenticator authenticator;

    @Override
    public void init(FilterConfig filterConfig) {
        ServletContext servletContext = filterConfig.getServletContext();
        App app = (App) servletContext.getAttribute(App.class.getName());
        this.authenticator = app.getAuthenticator();
    }


    /**
     * token как заглушка id пользователя
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                UserDto user = authenticator.authenticate(token);
                if (user != null) {
                    UserContext.setCurrentUser(user);
                }
            }

            chain.doFilter(request, response);

        } finally {
            UserContext.clear();
        }
    }
}

