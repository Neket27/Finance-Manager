package app.controller;

import app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void blockUser(@RequestParam String email) {
        userService.blockUser(email);
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam String email){
        userService.remove(email);
    }

}
