package com.grab.api.service.exception;

public class InvalidInputException extends RuntimeException {

  public InvalidInputException(String message) {
    super(message);
  }
}
