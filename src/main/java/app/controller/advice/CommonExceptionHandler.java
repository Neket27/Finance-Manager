package app.controller.advice;

import app.controller.advice.annotation.CustomExceptionHandler;
import app.exception.common.CreateException;
import app.exception.common.DeleteException;
import app.exception.common.NotFoundException;
import app.exception.common.UpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(annotations = CustomExceptionHandler.class)
public class CommonExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity handleException(Exception e) {
        return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CreateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleCreateException(CreateException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(UpdateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response handlerUpdateException(UpdateException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response handlerNotFoundException(NotFoundException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

    @ExceptionHandler(DeleteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response handlerDeleteException(DeleteException ex) {
        return new Response(ex.getMessage(), Instant.now().toString());
    }

}
