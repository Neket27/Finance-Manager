package app.servlet;

import app.dto.user.CreateUserDto;
import app.exception.UserAlreadyExistsException;
import app.service.AuthService;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/auth")
public class ServletAuth extends BaseServlet {

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
            authService.register(createUserDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);

        } catch (StreamReadException | DatabindException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format: " + e.getMessage());
        } catch (UserAlreadyExistsException e) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "User already exists");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
}
