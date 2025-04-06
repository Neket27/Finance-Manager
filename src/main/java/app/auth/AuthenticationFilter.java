package app.auth;

import app.dto.user.UserDto;
import app.entity.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import neket27.context.UserContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements Filter {

    private final Authenticator authenticator;

    @Override
    public void init(FilterConfig filterConfig) {

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
                User user = authenticator.authenticate(token);
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

