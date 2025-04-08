package app.controller;

import app.context.UserContext;
import app.controller.advice.annotation.CustomExceptionHandler;
import app.entity.Role;
import app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@CustomExceptionHandler
public class UserController {

    private final UserService userService;

    /**
     * Блокировка пользователя.
     *
     * Этот метод позволяет администратору заблокировать пользователя по его электронной почте.
     * Блокировка осуществляется только если у текущего пользователя роль ADMIN.
     *
     * @param email Электронная почта пользователя, которого необходимо заблокировать.
     */
    @Operation(summary = "Блокировка пользователя", description = "Метод для блокировки пользователя по его электронной почте. Доступен только для пользователей с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно заблокирован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Только для администраторов.")
    })
    @PostMapping("/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void blockUser(
            @RequestParam(name = "email") @Parameter(description = "Электронная почта пользователя, которого необходимо заблокировать.") String email) {
        if (UserContext.getCurrentUser().role().equals(Role.ADMIN)) {
            userService.blockUser(email);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен. Только для администраторов.");
        }
    }

    /**
     * Удаление пользователя.
     *
     * Этот метод позволяет администратору удалить пользователя по его электронной почте.
     * Удаление доступно только для пользователей с ролью ADMIN.
     *
     * @param email Электронная почта пользователя, которого необходимо удалить.
     */
    @Operation(summary = "Удаление пользователя", description = "Метод для удаления пользователя по его электронной почте. Доступен только для пользователей с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Только для администраторов.")
    })
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam(name = "email") @Parameter(description = "Электронная почта пользователя, которого необходимо удалить.") String email) {
        if (UserContext.getCurrentUser().role().equals(Role.ADMIN)) {
            userService.remove(email);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен. Только для администраторов.");
        }
    }
}
