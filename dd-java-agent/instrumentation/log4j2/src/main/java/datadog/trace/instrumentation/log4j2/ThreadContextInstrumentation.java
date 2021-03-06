package datadog.trace.instrumentation.log4j2;

import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.isTypeInitializer;
import static net.bytebuddy.matcher.ElementMatchers.named;

import com.google.auto.service.AutoService;
import datadog.trace.agent.tooling.Instrumenter;
import datadog.trace.agent.tooling.log.LogContextScopeListener;
import datadog.trace.agent.tooling.log.ThreadLocalWithDDTagsInitValue;
import datadog.trace.api.Config;
import datadog.trace.api.GlobalTracer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@AutoService(Instrumenter.class)
public class ThreadContextInstrumentation extends Instrumenter.Default {
  public static final String MDC_INSTRUMENTATION_NAME = "log4j";

  public ThreadContextInstrumentation() {
    super(MDC_INSTRUMENTATION_NAME);
  }

  @Override
  protected boolean defaultEnabled() {
    return Config.get().isLogsInjectionEnabled();
  }

  @Override
  public ElementMatcher<? super TypeDescription> typeMatcher() {
    return named("org.apache.logging.log4j.ThreadContext");
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    return singletonMap(
        isTypeInitializer(), ThreadContextInstrumentation.class.getName() + "$ThreadContextAdvice");
  }

  @Override
  public String[] helperClassNames() {
    return new String[] {
      LogContextScopeListener.class.getName(), ThreadLocalWithDDTagsInitValue.class.getName(),
    };
  }

  public static class ThreadContextAdvice {
    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void mdcClassInitialized(@Advice.Origin final Class<?> threadContextClass) {
      // @Advice.FieldValue("contextMap") Object contextMap
      try {
        final Method putMethod = threadContextClass.getMethod("put", String.class, String.class);
        final Method removeMethod = threadContextClass.getMethod("remove", String.class);
        GlobalTracer.get().addScopeListener(new LogContextScopeListener(putMethod, removeMethod));

        final Field contextMapField = threadContextClass.getDeclaredField("contextMap");
        contextMapField.setAccessible(true);
        Object contextMap = contextMapField.get(null);

        final Field localMapField = contextMap.getClass().getDeclaredField("localMap");
        localMapField.setAccessible(true);
        localMapField.set(contextMap, new ThreadLocalWithDDTagsInitValue());

      } catch (final NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
        org.slf4j.LoggerFactory.getLogger(threadContextClass)
            .debug("Failed to add log4j ThreadContext span listener", e);
      }
    }
  }
}
