import org.jspecify.annotations.NullMarked;

@NullMarked
open module com.grab.api {

  // Null-safety annotations
  requires org.jspecify;

  // Java
  requires java.sql;

  // Spring Boot
  requires spring.boot;
  requires spring.boot.autoconfigure;

  // Spring Core
  requires spring.context;
  requires spring.beans;
  requires spring.core;

  // Spring Web
  requires spring.web;
  requires spring.webmvc;

  // Spring Data
  requires spring.data.commons;
  requires spring.data.jdbc;
  requires spring.data.relational;
  requires spring.tx;

  // Spring Integration
  requires spring.messaging;
  requires spring.kafka;

  // Spring WebSocket
  requires spring.websocket;

  // Validation
  requires jakarta.validation;

  // API documentation
  requires io.swagger.v3.oas.annotations;

  // Logging
  requires org.slf4j;
  requires org.apache.tomcat.embed.core;

  // JSON processing
  requires tools.jackson.databind;
}
