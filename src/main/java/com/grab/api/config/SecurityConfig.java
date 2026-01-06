package com.grab.api.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Value("${jwt.public.key}")
  private RSAPublicKey publicKey;

  @Value("${jwt.private.key}")
  private RSAPrivateKey privateKey;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http.authorizeHttpRequests((authorize) -> authorize
            .requestMatchers("/v3/api-docs/**", "/scalar/**")
            .permitAll()
            .anyRequest()
            .authenticated())
        .csrf((csrf) -> csrf.ignoringRequestMatchers("/tokens"))
        .httpBasic(Customizer.withDefaults())
        .oauth2ResourceServer((jwt) -> jwt.jwt(Customizer.withDefaults()))
        .sessionManagement(
            (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  @Bean
  public UserDetailsService users(
      @Value("${app.user.master.username}") String username,
      @Value("${app.user.master.password}") String password) {

    return new InMemoryUserDetailsManager(User.withUsername(username)
        .password("{noop}" + password)
        .authorities("app")
        .build());
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    var jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
    var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwks);
  }
}
