package example.backend_mini_app.base.notifier;


import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DefaultNotifier implements Notifier {
    private final Sink sink; private final NotifierProperties props;
    public DefaultNotifier(Sink sink, NotifierProperties props) { this.sink = sink; this.props = props; }


    @Override public void info(String channel, String title, String message, Map<String, Object> meta) {
        if (ThreadLocalRandom.current().nextDouble() <= props.getSampleRate())
            sink.send(Notification.of(channel, "INFO", title, Truncator.clamp(message, props.getMaxBodyChars()), clampMeta(meta)));
    }
    @Override public void warn(String channel, String title, String message, Map<String, Object> meta) {
        sink.send(Notification.of(channel, "WARN", title, Truncator.clamp(message, props.getMaxBodyChars()), clampMeta(meta)));
    }
    @Override public void error(String channel, String title, String message, Map<String, Object> meta) {
        sink.send(Notification.of(channel, "ERROR", title, Truncator.clamp(message, props.getMaxBodyChars()), clampMeta(meta)));
    }


    private Map<String, Object> clampMeta(Map<String, Object> meta) {
        if (meta == null) return Map.of();
        return meta.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                e -> e.getKey(), e -> redactIfNeeded(e.getKey(), e.getValue())
        ));
    }
    private Object redactIfNeeded(String key, Object value) {
        if (key == null) return value;
        String k = key.toLowerCase();
        if (props.getRedactKeys().contains(k)) return "[REDACTED]";
        return Truncator.clampValue(value, props.getMaxFieldChars());
    }
}