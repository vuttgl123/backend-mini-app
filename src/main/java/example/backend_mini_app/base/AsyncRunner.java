package example.backend_mini_app.base;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

public class AsyncRunner {


    public static <T> CompletableFuture<Result<T>> submit(Task<T> task, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try { return Result.ok(task.run()); }
            catch (Throwable t) { return Result.fail(t); }
        }, executor);
    }


    public static CompletableFuture<Void> runAsync(Runnable r, Executor executor) {
        return CompletableFuture.runAsync(r, executor);
    }


    public static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> cf, Duration timeout) {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "async-timeout"); t.setDaemon(true); return t; });
        cf.whenComplete((r, t) -> ses.shutdown());
        CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
        ses.schedule(() -> timeoutFuture.completeExceptionally(new TimeoutException("Timeout after " + timeout)), timeout.toMillis(), TimeUnit.MILLISECONDS);
        return cf.applyToEither(timeoutFuture, Function.identity());
    }


    public static <I, O> CompletableFuture<List<O>> mapParallel(Collection<I> inputs,
                                                                Function<I, O> fn,
                                                                int concurrency,
                                                                Executor executor) {
        Objects.requireNonNull(inputs);
        Semaphore gate = new Semaphore(concurrency);
        List<CompletableFuture<O>> futures = new ArrayList<>();
        for (I in : inputs) {
            CompletableFuture<O> f = CompletableFuture.supplyAsync(() -> {
                try {
                    gate.acquire();
                    return fn.apply(in);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException(e);
                } finally { gate.release(); }
            }, executor);
            futures.add(f);
        }
        return sequence(futures);
    }


    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return all.thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }
}
