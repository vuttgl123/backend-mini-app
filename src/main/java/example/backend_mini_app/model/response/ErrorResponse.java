package example.backend_mini_app.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ErrorResponse {

    private String errorCode;
    private String message;
    private String serviceName;
    private int status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;
    private Map<String, Object> details;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    public ErrorResponse(String errorCode, String message, String serviceName,
                         int status, String path) {
        this();
        this.errorCode = errorCode;
        this.message = message;
        this.serviceName = serviceName;
        this.status = status;
        this.path = path;
    }

    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }
}
