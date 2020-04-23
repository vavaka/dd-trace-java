package datadog.trace.instrumentation.mulehttpconnector.server;

import datadog.trace.bootstrap.instrumentation.api.AgentScope;
import datadog.trace.bootstrap.instrumentation.api.AgentSpan;
import net.bytebuddy.asm.Advice;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;

import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activateSpan;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.propagate;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.startSpan;
import static datadog.trace.instrumentation.mulehttpconnector.server.ExtractAdapter.GETTER;
import static datadog.trace.instrumentation.mulehttpconnector.server.ServerDecorator.DECORATE;

public class ServerAdvice {
  static final String SPAN = "SPAN";
  static final String RESPONSE = "RESPONSE";

  @Advice.OnMethodEnter(suppress = Throwable.class)
  public static AgentScope onEnter(
      @Advice.This final Object source,
      @Advice.Argument(0) final FilterChainContext ctx,
      @Advice.Argument(1) final HttpHeader httpHeader) {

    if (ctx.getAttributes().getAttribute(SPAN) != null
        || !(httpHeader instanceof HttpRequestPacket)) {
      return null;
    }

    final HttpRequestPacket httpRequest = (HttpRequestPacket) httpHeader;
    final HttpResponsePacket httpResponse = httpRequest.getResponse();

    final AgentSpan.Context parentContext = propagate().extract(httpRequest, GETTER);
    final AgentSpan span = startSpan("grizzly.filterchain.server", parentContext);

    final AgentScope scope = activateSpan(span, false);
    System.out.println("scope has been activated in " + source.getClass().getName());

    DECORATE.afterStart(span);

    scope.setAsyncPropagation(true);

    ctx.getAttributes().setAttribute(SPAN, span);
    ctx.getAttributes().setAttribute(RESPONSE, httpResponse);

    return scope;
  }

  @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
  public static void onExit(
      @Advice.This final Object source,
      @Advice.Enter final AgentScope scope,
      @Advice.Argument(0) final FilterChainContext ctx,
      @Advice.Argument(1) final HttpHeader httpHeader,
      @Advice.Thrown final Throwable throwable) {

    final AgentSpan span = (AgentSpan) ctx.getAttributes().getAttribute(SPAN);

    if (scope == null || span == null) {
      return;
    }

    if (throwable == null) {
      final HttpRequestPacket httpRequest = (HttpRequestPacket) httpHeader;
      final HttpResponsePacket httpResponse = httpRequest.getResponse();
      DECORATE.onConnection(span, httpRequest);
      DECORATE.onRequest(span, httpRequest);
      DECORATE.onResponse(span, httpResponse);
      final TraceCompletionListener traceCompletionListener = new TraceCompletionListener();
      traceCompletionListener.setSpan(span);
      ctx.addCompletionListener(traceCompletionListener);
    } else {
      DECORATE.beforeFinish(span);
      DECORATE.onError(span, throwable);
      span.finish();
    }

    scope.close();
    System.out.println("scope has been deactivated in " + source.getClass().getName());
  }
}
