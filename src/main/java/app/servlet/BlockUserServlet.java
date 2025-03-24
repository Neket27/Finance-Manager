package app.servlet;

import app.aspect.exception.CustomExceptionHandler;
import app.container.Component;
import app.context.UserContext;
import app.dto.user.UserDto;
import app.entity.Role;
import app.service.UserService;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@Component
@CustomExceptionHandler
public class BlockUserServlet extends BaseServlet {

    private UserService userService;
    private JsonMapper jsonMapper;

    public BlockUserServlet(UserService userService, JsonMapper jsonMapper) {
        this.userService = userService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var node = jsonMapper.readTree(req.getInputStream());
        String emailToBlock = node.get("email").asText();

        resp.setContentType("application/json");

        if (isAdmin(UserContext.getCurrentUser())) {
            boolean success = userService.blockUser(emailToBlock);
            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(jsonMapper.writeValueAsString(Map.of("message", "Пользователь успешно заблокирован!")));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(jsonMapper.writeValueAsString(Map.of("error", "Не удалось заблокировать пользователя. Проверьте email.")));
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(jsonMapper.writeValueAsString(Map.of("error", "Нет прав для блокировки пользователей")));
        }
    }

    private boolean isAdmin(UserDto currentUser) {
        return currentUser.role().equals(Role.ADMIN);
    }
}

