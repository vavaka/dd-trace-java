package datadog.smoketest.springboot.controller;

import datadog.trace.api.interceptor.MutableSpan;
import io.opentracing.Span;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {
  @RequestMapping("/greeting")
  public String greeting() {
    {
      final Span span = io.opentracing.util.GlobalTracer.get().activeSpan();
      final long startTime;
      if (span instanceof MutableSpan) {
        startTime = ((MutableSpan) span).getLocalRootSpan().getStartTime();
      } else {
        startTime = System.currentTimeMillis();
      }
      span.setBaggageItem(
          "request.received", Long.toString(TimeUnit.NANOSECONDS.toMillis(startTime)));
    }

    {
      // Do this at the very end
      final Span span = io.opentracing.util.GlobalTracer.get().activeSpan();
      final String requestTime = span.getBaggageItem("request.received");
      if (requestTime != null) {
        final long startTime = Long.parseLong(requestTime);
        final long endTime = System.currentTimeMillis();
        span.setTag("request.processing.ms", endTime - startTime);
      }
    }

    return "Sup Dawg";
  }
}
