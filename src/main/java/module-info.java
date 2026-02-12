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

  // Spring Framework
  requires spring.context;
  requires spring.web;

  // Spring Data
  requires spring.data.commons;
  requires spring.data.relational;

  // Validation
  requires jakarta.validation;

  // API documentation
  requires io.swagger.v3.oas.annotations;

  // Logging
  requires org.slf4j;
}
