package com.grab.api.controller.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Extends {@link ResponseEntityExceptionHandler} so that all standard Spring MVC exceptions
 * (e.g. 404 Not Found, 405 Method Not Allowed, 415 Unsupported Media Type) are handled by the
 * base class with their correct HTTP semantics. A plain {@code @ExceptionHandler(Exception.class)}
 * without this base class would intercept those exceptions first and collapse them all into 500.
 * With the base class in place, the catch-all below only fires for exceptions the framework has
 * no specific handler for.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    var fieldErrors = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .toList();

    var problemDetail = ProblemDetail.forStatusAndDetail(status, "Invalid request content.");
    problemDetail.setProperty("fieldErrors", fieldErrors);

    return ResponseEntity.status(status).body(problemDetail);
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ProblemDetail handleDuplicateKey() {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Resource already exist.");
  }
}
