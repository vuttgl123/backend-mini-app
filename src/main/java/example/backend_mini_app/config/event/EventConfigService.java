package example.backend_mini_app.config.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.backend_mini_app.config.EventConfig;
import example.backend_mini_app.exception.ErrorCode;
import example.backend_mini_app.shared.helper.ErrorHelper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventConfigService {

    private static final String CONFIG_PATH = "data/event_config.json";

    private final ObjectMapper objectMapper;
    private volatile List<EventConfig> cachedEvents;

    @PostConstruct
    public void init() {
        try {
            loadConfigs();
            log.info("Loaded {} event configurations successfully", cachedEvents.size());
        } catch (Exception e) {
            log.error("Failed to load event configurations on startup", e);
            cachedEvents = Collections.emptyList();
        }
    }

    public List<EventConfig> getAll() {
        if (cachedEvents == null) {
            synchronized (this) {
                if (cachedEvents == null) {
                    loadConfigs();
                }
            }
        }
        return Collections.unmodifiableList(cachedEvents);
    }

    public Optional<EventConfig> findByCode(String code) {
        return getAll().stream()
                .filter(event -> event.code().equals(code))
                .findFirst();
    }

    public List<EventConfig> getAnnualEvents() {
        return getAll().stream()
                .filter(EventConfig::repeatAnnually)
                .toList();
    }

    public List<EventConfig> getEventsByYear(int year) {
        return getAll().stream()
                .filter(event -> event.repeatAnnually() || (event.year() != null && event.year() == year))
                .toList();
    }


    public synchronized void reload() {
        log.info("Reloading event configurations...");
        loadConfigs();
        log.info("Reloaded {} event configurations", cachedEvents.size());
    }

    private void loadConfigs() {
        try {
            List<EventConfig> events = loadFromClasspath();
            validateConfigs(events);
            cachedEvents = new ArrayList<>(events);
        } catch (IOException e) {
            log.error("Failed to load event config from {}", CONFIG_PATH, e);
            throw ErrorHelper.ex(ErrorCode.EVENT_CONFIG_IO_ERROR, e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid event config data", e);
            throw ErrorHelper.ex(ErrorCode.EVENT_CONFIG_INVALID_DATA, e);
        }
    }

    private List<EventConfig> loadFromClasspath() throws IOException {
        try (InputStream is = new ClassPathResource(EventConfigService.CONFIG_PATH).getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<List<EventConfig>>() {});
        }
    }

    private void validateConfigs(List<EventConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            throw ErrorHelper.ex(ErrorCode.EVENT_CONFIG_EMPTY);
        }

        long uniqueCodes = configs.stream()
                .map(EventConfig::code)
                .distinct()
                .count();

        if (uniqueCodes != configs.size()) {
            throw ErrorHelper.ex(ErrorCode.EVENT_CONFIG_DUPLICATE_CODE);
        }
    }
}