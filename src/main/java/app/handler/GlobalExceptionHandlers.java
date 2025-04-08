package app.handler;

import app.aspect.exception.ExceptionHandler;
import app.aspect.exception.ResponseStatus;
import app.exception.ErrorLogoutException;
import app.exception.NotFoundException;
import app.exception.TransactionException;
import app.exception.auth.ErrorLoginExeption;
import app.exception.auth.ErrorRegisterExeption;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;

public class GlobalExceptionHandlers {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
    public Response handleRuntime(RuntimeException ex) {
        return new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong: " + ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(DatabindException.class)
    @ResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
    public Response handlerParseJson(DatabindException ex) {
        return new Response(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(StreamReadException.class)
    @ResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
    public Response handlerParseJson(StreamReadException ex) {
        return new Response(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format: " + ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
    public Response handleNotFound(TransactionException ex) {
        return new Response(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage(), Instant.now());
    }


    /**
     * Auth handlers
     */

    @ExceptionHandler(ErrorRegisterExeption.class)
    @ResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
    public Response handleRegister(ErrorRegisterExeption ex) {
        return new Response(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(ErrorLoginExeption.class)
    @ResponseStatus(HttpServletResponse.SC_UNAUTHORIZED)
    public Response handleLogin(ErrorLoginExeption ex) {
        return new Response(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(ErrorLogoutException.class)
    @ResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
    public Response handleLogout(ErrorLoginExeption ex) {
        return new Response(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage(), Instant.now());
    }

    /**
     * Transaction handler
     */

    @ExceptionHandler(TransactionException.class)
    @ResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
    public Response handleTransaction(TransactionException ex) {
        return new Response(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage(), Instant.now());
    }



}
