package com.grab.api.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

@TestConfiguration
public class TestSchedulingConfig {

  @Bean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
  public ScheduledAnnotationBeanPostProcessor scheduledAnnotationBeanPostProcessor() {
    return new ScheduledAnnotationBeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
      }
    };
  }
}
