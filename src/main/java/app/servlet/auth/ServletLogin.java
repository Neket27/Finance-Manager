package app.servlet.auth;

import app.dto.auth.Signin;
import app.dto.user.UserDto;
import app.exception.auth.ErrorLoginExeption;
import app.handler.Response;
import app.service.AuthService;
import app.servlet.BaseServlet;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

@WebServlet("/api/v1/auth/signin")
public class ServletLogin extends BaseServlet {

    private AuthService authService;
    private JsonMapper mapper;

    @Override
    public void init(ServletConfig config) {
        super.init(config);
        this.authService = app.getAuthService();
        this.mapper = app.getJsonMapper();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Signin signin = mapper.readValue(req.getInputStream(), Signin.class);
            UserDto userDto = authService.login(signin);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(mapper.writeValueAsString(userDto));

        }catch (ErrorLoginExeption e){
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Response response =new Response(HttpServletResponse.SC_UNAUTHORIZED,e.getMessage(), Instant.now());
            resp.getWriter().write(mapper.writeValueAsString(response));

        } catch (StreamReadException | DatabindException e) {
            Response response =new Response(HttpServletResponse.SC_UNAUTHORIZED,"Invalid JSON format: " + e.getMessage(), Instant.now());
            resp.getWriter().write(mapper.writeValueAsString(response));
        } catch (Exception e) {
            Response response =new Response(HttpServletResponse.SC_UNAUTHORIZED,e.getMessage(), Instant.now());
            resp.getWriter().write(mapper.writeValueAsString(response));
        }
    }
}

