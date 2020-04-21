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
import static datadog.trace.instrumentation.mulehttpconnector.server.TraceCompletionListener.LISTENER;

public class ServerAdvice {
  static final String SPAN = "SPAN";
  static final String RESPONSE = "RESPONSE";

  @Advice.OnMethodEnter(suppress = Throwable.class)
  public static AgentScope onEnter(
      @Advice.This final Object source,
      @Advice.Argument(0) final FilterChainContext ctx,
      @Advice.Argument(1) final HttpHeader httpHeader) {

    if (ctx.getAttributes().getAttribute(SPAN) != null) {
      return null;
    }

    final HttpRequestPacket httpRequest = (HttpRequestPacket) httpHeader;
    final HttpResponsePacket httpResponse = httpRequest.getResponse();

    final AgentSpan.Context parentContext = propagate().extract(httpRequest, GETTER);
    final AgentSpan span = startSpan("http.request", parentContext);

    final AgentScope scope = activateSpan(span, false);
    scope.setAsyncPropagation(true);

    DECORATE.afterStart(span);

    DECORATE.onConnection(span, httpRequest);
    DECORATE.onRequest(span, httpRequest);
    DECORATE.onResponse(span, httpResponse);

    ctx.getAttributes().setAttribute(SPAN, span);
    ctx.getAttributes().setAttribute(RESPONSE, httpResponse);

    return scope;
  }

  @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
  public static void onExit(
      @Advice.Enter final AgentScope scope,
      @Advice.Argument(0) final FilterChainContext ctx,
      @Advice.Thrown final Throwable throwable) {

    final AgentSpan span = (AgentSpan) ctx.getAttributes().getAttribute(SPAN);

    if (scope == null || span == null) {
      return;
    }

    if (throwable == null) {
      LISTENER.setSpan(span);
      ctx.addCompletionListener(LISTENER);
    } else {
      DECORATE.beforeFinish(span);
      DECORATE.onError(span, throwable);
      span.finish();
    }
    //    scope.close();
  }
}
