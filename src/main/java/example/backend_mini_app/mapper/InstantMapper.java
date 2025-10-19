package example.backend_mini_app.mapper;

import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class InstantMapper {
    public Long toEpochSecond(Instant instant) {
        return instant == null ? null : instant.getEpochSecond();
    }
    public Instant fromEpochSecond(Long epochSecond) {
        return epochSecond == null ? null : Instant.ofEpochSecond(epochSecond);
    }
}
