import org.jspecify.annotations.NullMarked;

@NullMarked
open module com.grab.api {
  requires org.jspecify;
  requires spring.boot.autoconfigure;
  requires spring.boot;
}
