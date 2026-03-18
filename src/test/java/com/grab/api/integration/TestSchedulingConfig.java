package com.grab.api.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.NoOpTaskScheduler;

@TestConfiguration
public class TestSchedulingConfig {

  @Bean
  public TaskScheduler taskScheduler() {
    return new NoOpTaskScheduler();
  }
}
