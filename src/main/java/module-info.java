import org.jspecify.annotations.NullMarked;

@NullMarked
open module com.grab.api {

  // Required to start application
  requires org.apache.tomcat.embed.core;

  // Null-safety annotations
  requires org.jspecify;

  // Spring Boot
  requires spring.boot;
  requires spring.boot.autoconfigure;

  // Spring Core
  requires spring.context;
  requires spring.beans;

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

  // Validation
  requires jakarta.validation;

  // API documentation
  requires io.swagger.v3.oas.annotations;

  // Logging
  requires org.slf4j;
}
