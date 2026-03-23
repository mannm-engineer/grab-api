package com.grab.api.service.exception;

public class DomainNotFoundException extends RuntimeException {

  private final String domainId;

  public DomainNotFoundException(String domainId) {
    this.domainId = domainId;
  }

  public String getDomainId() {
    return domainId;
  }
}
