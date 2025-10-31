package example.backend_mini_app.base.notifier;

import java.util.Map;

public interface Notifier {
    void info(String channel, String title, String message, Map<String, Object> meta);
    void warn(String channel, String title, String message, Map<String, Object> meta);
    void error(String channel, String title, String message, Map<String, Object> meta);
}
