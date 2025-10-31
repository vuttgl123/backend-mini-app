package example.backend_mini_app.base.notifier;

import java.time.Instant;
import java.util.Map;

public record Notification(
        String channel, // e.g. "http", "event", "job"
        String level, // INFO/WARN/ERROR
        String title, // short summary
        String message, // main content (already truncated by Notifier)
        Map<String, Object> meta, // structured fields
        Instant timestamp
) {
    public static Notification of(String channel, String level, String title, String message, Map<String, Object> meta) {
        return new Notification(channel, level, title, message, meta, Instant.now());
    }
}
