package app.controller.advice;

import app.controller.advice.annotation.CustomExceptionHandler;
import app.exception.auth.ErrorLogoutException;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorRegistrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(annotations = CustomExceptionHandler.class)
@Slf4j
public class AuthExceptionHandler {

    @ExceptionHandler(ErrorRegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleRegister(ErrorRegistrationException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(ErrorLoginExeption.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response handleLogin(ErrorLoginExeption ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(ErrorLogoutException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response handleLogout(ErrorLogoutException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

}
