package com.grab.api.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

@TestConfiguration
public class TestSchedulingConfig {

  @Bean
  public ScheduledAnnotationBeanPostProcessor scheduledAnnotationBeanPostProcessor() {
    return new ScheduledAnnotationBeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) {
        // Skip scheduling registration — beans still exist but @Scheduled won't trigger
        return bean;
      }
    };
  }
}
