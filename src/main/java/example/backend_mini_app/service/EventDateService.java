package example.backend_mini_app.service;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import example.backend_mini_app.config.EventConfig;
import example.backend_mini_app.config.event.EventConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventDateService {
    private final EventConfigService eventConfigService;

    public LocalDate toSolarDateForYear(EventConfig e, int targetYear) {
        int year = e.repeatAnnually() ? targetYear : e.year();
        if ("LUNAR".equalsIgnoreCase(String.valueOf(e.type()))) {
            int lunarMonth = e.leapMonth() ? -e.month() : e.month();
            Solar s = Lunar.fromYmd(year, lunarMonth, e.day()).getSolar();
            return LocalDate.of(s.getYear(), s.getMonth(), s.getDay());
        } else {
            return LocalDate.of(year, e.month(), e.day());
        }
    }

    public List<EventView> listForYear(int year) {
        return eventConfigService.getAll().stream()
                .map(e -> new EventView(
                        e.code(), e.name(), String.valueOf(e.type()), toSolarDateForYear(e, year)))
                .sorted(Comparator.comparing(EventView::date))
                .collect(Collectors.toList());
    }

    public List<EventView> eventsOn(LocalDate date) {
        return listForYear(date.getYear()).stream()
                .filter(v -> v.date().isEqual(date))
                .collect(Collectors.toList());
    }

    public List<EventView> upcoming(int limit) {
        LocalDate today = LocalDate.now();
        List<EventView> thisYear = listForYear(today.getYear());
        List<EventView> nextYear  = listForYear(today.plusYears(1).getYear());
        return thisYear.stream().filter(v -> !v.date().isBefore(today))
                        .toList()
                        .stream()
                        .sorted(Comparator.comparing(EventView::date))
                        .limit(limit)
                        .collect(Collectors.toList());
    }

    public record EventView(String code, String name, String type, LocalDate date) {}
}
