import org.jspecify.annotations.NullMarked;

@NullMarked
open module com.grab.api {

  // Null-safety annotations
  requires org.jspecify;

  // Spring Boot
  requires spring.boot.autoconfigure;
  requires spring.boot;

  // Spring Framework
  requires spring.web;
  requires spring.context;
}
