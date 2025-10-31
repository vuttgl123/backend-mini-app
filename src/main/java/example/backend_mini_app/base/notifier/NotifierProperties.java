package example.backend_mini_app.base.notifier;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "core.notifier")
public class NotifierProperties {
    /** Max length for request/response body to log */
    private int maxBodyChars = 2000;
    /** Max length for headers/params rendered as text */
    private int maxFieldChars = 256;
    /** Sample rate 0.0-1.0, apply to INFO level only */
    private double sampleRate = 1.0;
    /** Redact keys (case-insensitive) */
    private Set<String> redactKeys = Set.of("authorization","cookie","set-cookie","password","secret","token","apikey","api-key");
    /** Whether to log response bodies */
    private boolean logResponseBody = true;


    public int getMaxBodyChars() { return maxBodyChars; }
    public void setMaxBodyChars(int v) { this.maxBodyChars = v; }
    public int getMaxFieldChars() { return maxFieldChars; }
    public void setMaxFieldChars(int v) { this.maxFieldChars = v; }
    public double getSampleRate() { return sampleRate; }
    public void setSampleRate(double v) { this.sampleRate = v; }
    public Set<String> getRedactKeys() { return redactKeys; }
    public void setRedactKeys(Set<String> redactKeys) { this.redactKeys = redactKeys; }
    public boolean isLogResponseBody() { return logResponseBody; }
    public void setLogResponseBody(boolean logResponseBody) { this.logResponseBody = logResponseBody; }
}
