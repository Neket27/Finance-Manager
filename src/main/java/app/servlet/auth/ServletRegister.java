package app.servlet.auth;

import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.exception.UserAlreadyExistsException;
import app.exception.auth.ErrorRegisterExeption;
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

@WebServlet("/api/v1/auth/signup")
public class ServletRegister extends BaseServlet {

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
            CreateUserDto createUserDto = mapper.readValue(req.getInputStream(), CreateUserDto.class);
            UserDto userDto = authService.register(createUserDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write(mapper.writeValueAsString(userDto));

        } catch (ErrorRegisterExeption e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            Response response =new Response(HttpServletResponse.SC_CONFLICT,e.getMessage(), Instant.now());
            resp.getWriter().write(mapper.writeValueAsString(response));

        } catch (StreamReadException | DatabindException e) {
            Response response =new Response(HttpServletResponse.SC_BAD_REQUEST,"Invalid JSON format: " + e.getMessage(), Instant.now());
            resp.getWriter().write(mapper.writeValueAsString(response));
        } catch (UserAlreadyExistsException e) {
            Response response =new Response(HttpServletResponse.SC_CONFLICT,"User already exist: " + e.getMessage(), Instant.now());
            resp.getWriter().write(mapper.writeValueAsString(response));
        } catch (Exception e) {
            Response response =new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Server error: " + e.getMessage(), Instant.now());
            resp.getWriter().write(mapper.writeValueAsString(response));
        }
    }



}
