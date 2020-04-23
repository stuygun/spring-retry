package com.tuygun.sandbox.spring.retry.configuration;

import com.tuygun.sandbox.spring.retry.listeners.DefaultListenerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
public class RetryConfiguration {
  @Bean
  public DefaultListenerSupport defaultListenerSupport(){
    return new DefaultListenerSupport();
  }

  @Bean
  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();

    FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(500L);
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(5);
    retryTemplate.setRetryPolicy(retryPolicy);

    retryTemplate.registerListener(defaultListenerSupport());
    return retryTemplate;
  }
}
