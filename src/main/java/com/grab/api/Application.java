package com.grab.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public CorsFilter corsFilter() {
    var config = new CorsConfiguration();
    config.addAllowedOriginPattern("*"); // allow all origins
    config.addAllowedHeader("*");        // allow all headers
    config.addAllowedMethod("*");        // allow all HTTP methods
    config.setAllowCredentials(true);    // allow cookies/authorization headers if needed

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsFilter(source);
  }
}
