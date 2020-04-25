package com.tuygun.sandbox.spring.retry.listeners;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;

@Slf4j
public class DefaultListenerSupport extends RetryListenerSupport {
  @Override
  public <T, E extends Throwable> void close(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    if (context.getAttribute(RetryContext.RECOVERED) != null
        && ((Boolean) context.getAttribute(RetryContext.RECOVERED))) {
      log.warn(
          "On-Close: After #retry-attempt({}), retryable method {} is RECOVERED with recovery method.",
          context.getRetryCount(),
          context.getAttribute(RetryContext.NAME));
    } else {
      if (context.getAttribute(RetryContext.EXHAUSTED) != null
          && ((Boolean) context.getAttribute(RetryContext.EXHAUSTED))) {
        log.warn(
            "On-Close: After #retry-attempt({}), retryable method {} is UNABLE to recover from exception {}",
            context.getRetryCount(),
            context.getAttribute(RetryContext.NAME),
            ExceptionUtils.getStackTrace(throwable));
      } else {
        log.warn(
            "On-Close: After #retry-attempt({}), retryable method {} is RECOVERED with retry.",
            context.getRetryCount(),
            context.getAttribute(RetryContext.NAME));
      }
    }
    super.close(context, callback, throwable);
  }

  @Override
  public <T, E extends Throwable> void onError(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    log.warn(
        "On-Error: Retry attempt:{} for retryable method {} threw exception {}",
        context.getRetryCount(),
        context.getAttribute(RetryContext.NAME),
        ExceptionUtils.getStackTrace(throwable));
    super.onError(context, callback, throwable);
  }

  @Override
  public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
    log.warn("On-Open: Exception occurred, Retry-Session started");
    return super.open(context, callback);
  }
}
