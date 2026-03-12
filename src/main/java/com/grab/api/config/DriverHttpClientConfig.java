package com.grab.api.config;

import com.grab.api.repository.DriverHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class DriverHttpClientConfig {

  @Bean
  DriverHttpClient driverHttpClient(@Value("${driver.api.base-url}") String baseUrl) {
    var restClient = RestClient.builder().baseUrl(baseUrl).build();
    var adapter = RestClientAdapter.create(restClient);
    var factory = HttpServiceProxyFactory.builderFor(adapter).build();
    return factory.createClient(DriverHttpClient.class);
  }
}
