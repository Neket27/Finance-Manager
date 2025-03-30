package app.controller;

import app.controller.advice.annotation.CustomExceptionHandler;
import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CustomExceptionHandler
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody CreateUserDto dto) {
        return authService.register(dto);
    }

    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    public ResponseLogin login(@Valid @RequestBody SignIn signIn) {
        return authService.login(signIn);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout() {
        authService.logout();
    }

}
