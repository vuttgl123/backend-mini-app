package example.backend_mini_app.base.notifier;

import java.util.List;

public class CompositeSink implements Sink {
    private final List<Sink> sinks;
    public CompositeSink(List<Sink> sinks) { this.sinks = sinks; }
    @Override public void send(Notification n) { sinks.forEach(s -> s.send(n)); }
}
