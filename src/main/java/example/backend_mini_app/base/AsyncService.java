package example.backend_mini_app.base;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class AsyncService {
    private final Executor io;
    private final Executor cpu;


    public AsyncService(@Qualifier("ioExecutor") Executor io,
                        @Qualifier("cpuExecutor") Executor cpu) {
        this.io = io; this.cpu = cpu;
    }


    public <T> CompletableFuture<T> supply(Task<T> task, RunOn runOn) {
        return CompletableFuture.supplyAsync(() -> {
            try { return task.run(); } catch (Exception e) { throw new RuntimeException(e); }
        }, resolve(runOn));
    }


    public CompletableFuture<Void> run(Runnable r, RunOn runOn) {
        return CompletableFuture.runAsync(r, resolve(runOn));
    }


    private Executor resolve(RunOn runOn) { return runOn == RunOn.CPU ? cpu : io; }

    // ===================== HOW TO USE (concurrency) =====================
// 1) Đồng bộ – khoá và retry
// var lock = Locks.newReentrantLock();
// String value = SyncRunner.withLock(lock, () -> expensiveSync());
// String value2 = SyncRunner.retry(3, Duration.ofMillis(200), () -> maybeFlaky());
//
// 2) Bất đồng bộ – chạy trên pool IO hoặc CPU
// @Service
// public class ReportService {
// private final AsyncService asyncService;
// public ReportService(AsyncService asyncService) { this.asyncService = asyncService; }
//
// public CompletableFuture<Report> generateAsync(Long id) {
// return asyncService.supply(() -> loadAndRender(id), RunOn.IO);
// }
// }
//
// 3) Chạy song song với giới hạn đồng thời
// List<Result> out = AsyncRunner.mapParallel(ids, this::processOne, 8, ioExecutor).get();
//
// 4) Timeout cho tác vụ async
// CompletableFuture<String> cf = asyncService.supply(this::slowCall, RunOn.IO);
// String s = AsyncRunner.withTimeout(cf, Duration.ofSeconds(3)).join();

}