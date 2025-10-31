package example.backend_mini_app.base;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

public class SyncRunner {


    public static <T> T withLock(Lock lock, Callable<T> callable) {
        lock.lock();
        try { return callable.call(); }
        catch (RuntimeException re) { throw re; }
        catch (Exception e) { throw new RuntimeException(e); }
        finally { lock.unlock(); }
    }


    public static <T> T withTryLock(Lock lock, Duration timeout, Callable<T> callable) {
        boolean acquired = false;
        try {
            acquired = lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) throw new TimeoutException("Failed to acquire lock within " + timeout);
            return callable.call();
        } catch (TimeoutException te) { throw new RejectedExecutionException(te); }
        catch (RuntimeException re) { throw re; }
        catch (Exception e) { throw new RuntimeException(e); }
        finally { if (acquired) lock.unlock(); }
    }


    public static <T> T retry(int maxAttempts, Duration delay, Callable<T> callable) {
        RuntimeException last = null;
        for (int i = 1; i <= maxAttempts; i++) {
            try { return callable.call(); }
            catch (RuntimeException re) { last = re; }
            catch (Exception e) { last = new RuntimeException(e); }
            try { Thread.sleep(delay.toMillis()); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new RuntimeException(ie); }
        }
        throw last == null ? new RuntimeException("Unknown error") : last;
    }
}
