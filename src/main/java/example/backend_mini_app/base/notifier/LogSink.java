package example.backend_mini_app.base.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogSink implements Sink {
    private static final Logger log = LoggerFactory.getLogger(LogSink.class);
    @Override public void send(Notification n) {
        String line = "[" + n.channel() + "] " + n.level() + " " + n.title() + " | " + n.message() + " | meta=" + n.meta();
        switch (n.level()) { case "ERROR" -> log.error(line); case "WARN" -> log.warn(line); default -> log.info(line); }
    }
}
