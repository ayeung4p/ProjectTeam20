package library.controllers;

import org.junit.jupiter.api.extension.*;
import javafx.application.Platform;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class JavaFXLifeCycleExtension implements BeforeAllCallback, InvocationInterceptor {

    private static final AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public void beforeAll(ExtensionContext ctx) throws Exception {
        if (started.compareAndSet(false, true)) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            if (!latch.await(15, TimeUnit.SECONDS))
                throw new IllegalStateException("FX startup timeout");
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> ctx,
                                    ExtensionContext extCtx) throws Throwable {
        synchronized (JavaFXLifeCycleExtension.class) {
            invocation.proceed();
        }
    }

    @SuppressWarnings("CatchMayIgnoreException")
    public static void runAndWait(Runnable task) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> fail = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                fail.set(t);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(10, TimeUnit.SECONDS))
            throw new RuntimeException("FX thread timeout (10 s)");

        Throwable t = fail.get();
        if (t != null) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new RuntimeException("FX task threw", t);
        }
    }
}
