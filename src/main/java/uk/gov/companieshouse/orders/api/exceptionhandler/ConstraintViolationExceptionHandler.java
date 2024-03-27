package uk.gov.companieshouse.orders.api.exceptionhandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.IOException;
import java.util.stream.Collectors;

@ControllerAdvice
public class ConstraintViolationExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ConstraintValidationError> handleConstraintViolationException(ConstraintViolationException exception,
                                                                                        ServletWebRequest webRequest) throws IOException {
        ConstraintValidationError constraintViolationError = new ConstraintValidationError();
        constraintViolationError.setStatus(HttpStatus.BAD_REQUEST);
        constraintViolationError.setConstraintErrors(exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(constraintViolationError);
    }
}