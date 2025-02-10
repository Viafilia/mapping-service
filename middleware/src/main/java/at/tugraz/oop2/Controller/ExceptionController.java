package at.tugraz.oop2.Controller;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.HttpServerErrorException;

@Getter
@Setter
class ErrorResponse {
    private String message;
    private int statusCode;
    
    public ErrorResponse(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode.value();
    }
}

@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler({MethodArgumentNotValidException.class, 
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleExceptionBadRequest(
            Exception e) {
        return new ResponseEntity<>(new ErrorResponse(
                e.getMessage(), 
                HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleExceptionNotFound(
            NoSuchElementException e) {
        return new ResponseEntity<>(new ErrorResponse(
                e.getMessage(), 
                HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError(HttpServerErrorException e) {
        return new ResponseEntity<>(new ErrorResponse(
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
