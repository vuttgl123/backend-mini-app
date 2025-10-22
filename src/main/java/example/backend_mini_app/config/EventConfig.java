package example.backend_mini_app.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record EventConfig(@NotBlank(message = "Event code cannot be blank") String code,
                          @NotBlank(message = "Event name cannot be blank") String name,
                          @NotNull(message = "Event type cannot be null") EventType type, Integer year,
                          @Min(value = 1, message = "Month must be between 1 and 12") @Max(value = 12, message = "Month must be between 1 and 12") int month,
                          @Min(value = 1, message = "Day must be between 1 and 31") @Max(value = 31, message = "Day must be between 1 and 31") int day,
                          boolean leapMonth, boolean repeatAnnually) {

    @JsonCreator
    public EventConfig(
            @JsonProperty("code") String code,
            @JsonProperty("name") String name,
            @JsonProperty("type") EventType type,
            @JsonProperty("year") Integer year,
            @JsonProperty("month") int month,
            @JsonProperty("day") int day,
            @JsonProperty("leapMonth") boolean leapMonth,
            @JsonProperty("repeatAnnually") boolean repeatAnnually) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.year = year;
        this.month = month;
        this.day = day;
        this.leapMonth = leapMonth;
        this.repeatAnnually = repeatAnnually;

        validate();
    }

    private void validate() {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Event code cannot be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Event name cannot be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (!repeatAnnually && year == null) {
            throw new IllegalArgumentException("Year is required for non-repeating events");
        }
        if (type == EventType.LUNAR && month == 12 && day > 30) {
            throw new IllegalArgumentException("Lunar month 12 cannot have more than 30 days");
        }
    }

    public enum EventType {
        SOLAR,
        LUNAR
    }
}