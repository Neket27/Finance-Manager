package app.controller;

import app.dto.auth.ResponseLogin;
import app.dto.auth.SignIn;
import app.dto.user.CreateUserDto;
import app.dto.user.UserDto;
import app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public UserDto register(@RequestBody CreateUserDto dto) {
        return authService.register(dto);
    }

    @PostMapping("/signin")
    public ResponseLogin login(@RequestBody SignIn signIn) {
        return authService.login(signIn);
    }

    @PostMapping("/logout")
    public void logout() {
        authService.logout();
    }

}
