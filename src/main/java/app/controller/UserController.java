package app.controller;

import app.context.UserContext;
import app.controller.advice.annotation.CustomExceptionHandler;
import app.entity.Role;
import app.service.UserService;
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

    @PostMapping("/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void blockUser(@RequestParam(name = "email") String email) {
        if (UserContext.getCurrentUser().role().equals(Role.ADMIN))
            userService.blockUser(email);
        else
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam(name = "email") String email) {
        if (UserContext.getCurrentUser().role().equals(Role.ADMIN))
            userService.remove(email);
        else
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
