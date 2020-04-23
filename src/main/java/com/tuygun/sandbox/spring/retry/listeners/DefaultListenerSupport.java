package com.tuygun.sandbox.spring.retry.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;

@Slf4j
public class DefaultListenerSupport extends RetryListenerSupport {
  @Override
  public <T, E extends Throwable> void close(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    log.info("On-Close: #attempt:{}", context.getRetryCount());
    super.close(context, callback, throwable);
  }

  @Override
  public <T, E extends Throwable> void onError(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    log.error("On-Error: #attempt:{}", context.getRetryCount());
    super.onError(context, callback, throwable);
  }

  @Override
  public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
    log.info("On-Open: #attempt:{}", context.getRetryCount());
    return super.open(context, callback);
  }
}
