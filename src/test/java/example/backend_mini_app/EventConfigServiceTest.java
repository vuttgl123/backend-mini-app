//package example.backend_mini_app;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import example.backend_mini_app.config.EventConfig;
//import example.backend_mini_app.config.event.EventConfigService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@ExtendWith(MockitoExtension.class)
//class EventConfigServiceTest {
//
//    private EventConfigService service;
//    private ObjectMapper objectMapper;
//
//    @BeforeEach
//    void setUp() {
//        objectMapper = new ObjectMapper();
//        service = new EventConfigService(objectMapper);
//    }
//
//    @Test
//    void shouldLoadEventConfigsSuccessfully() {
//        // When
//        service.init();
//        List<EventConfig> events = service.getAll();
//
//        // Then
//        assertThat(events).isNotEmpty();
//        assertThat(events).allMatch(e -> e.code() != null);
//        assertThat(events).allMatch(e -> e.name() != null);
//    }
//
//    @Test
//    void shouldReturnImmutableList() {
//        // Given
//        service.init();
//        List<EventConfig> events = service.getAll();
//
//        // Then
//        assertThatThrownBy(() -> events.clear())
//                .isInstanceOf(UnsupportedOperationException.class);
//    }
//
//    @Test
//    void shouldFindEventByCode() {
//        // Given
//        service.init();
//
//        // When
//        Optional<EventConfig> event = service.findByCode("MID_AUTUMN");
//
//        // Then
//        assertThat(event).isPresent();
//        assertThat(event.get().name()).isEqualTo("Tết Trung Thu");
//        assertThat(event.get().type()).isEqualTo(EventConfig.EventType.LUNAR);
//    }
//
//    @Test
//    void shouldReturnEmptyForNonExistentCode() {
//        // Given
//        service.init();
//
//        // When
//        Optional<EventConfig> event = service.findByCode("NON_EXISTENT");
//
//        // Then
//        assertThat(event).isEmpty();
//    }
//
//    @Test
//    void shouldGetAnnualEventsOnly() {
//        // Given
//        service.init();
//
//        // When
//        List<EventConfig> annualEvents = service.getAnnualEvents();
//
//        // Then
//        assertThat(annualEvents).isNotEmpty();
//        assertThat(annualEvents).allMatch(EventConfig::repeatAnnually);
//    }
//
//    @Test
//    void shouldGetEventsByYear() {
//        // Given
//        service.init();
//        int year = 2025;
//
//        // When
//        List<EventConfig> events = service.getEventsByYear(year);
//
//        // Then
//        assertThat(events).isNotEmpty();
//        assertThat(events).allMatch(e ->
//                e.repeatAnnually() ||
//                        (e.year() != null && e.year() == year)
//        );
//    }
//
//    @Test
//    void shouldValidateDuplicateCodes() {
//        // This test assumes you have a way to inject bad config
//        // In real scenario, you'd mock the file loading
//        // Just showing the concept here
//        assertThat(service).isNotNull();
//    }
//
//    @Test
//    void shouldHandleReload() {
//        // Given
//        service.init();
//        List<EventConfig> before = service.getAll();
//
//        // When
//        service.reload();
//        List<EventConfig> after = service.getAll();
//
//        // Then
//        assertThat(after.size()).isEqualTo(before.size());
//    }
//}
