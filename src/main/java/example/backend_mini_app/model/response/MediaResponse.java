package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MediaResponse {
    private Long id;
    private String objectKey;
    private String mimeType;
    private long sizeBytes;
    private String url;
    private LocalDateTime createdAt;
}
