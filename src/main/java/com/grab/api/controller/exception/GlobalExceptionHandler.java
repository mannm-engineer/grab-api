package com.grab.api.controller.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
  protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    var problemDetail =
        createProblemDetail(ex, status, "Invalid request content.", null, null, request);

    var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .toList();
    problemDetail.setProperty("fieldErrors", fieldErrors);

    return handleExceptionInternal(ex, problemDetail, headers, status, request);
  }
}
