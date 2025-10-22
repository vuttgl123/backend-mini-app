package example.backend_mini_app.shared.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NotificationHelper {
    private static final Logger log = LoggerFactory.getLogger(NotificationHelper.class);
    private NotificationHelper() {}

    public static void info(String code, String message, Object ctx) {
        log.info("[{}] {} - ctx={}", code, message, ctx);
    }

    public static void error(String code, String message, Throwable t) {
        log.error("[{}] {}", code, message, t);
    }
}
