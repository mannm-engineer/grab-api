package com.grab.api.service.exception;

public class DomainNotFoundException extends RuntimeException {

  public DomainNotFoundException(String message) {
    super(message);
  }
}
