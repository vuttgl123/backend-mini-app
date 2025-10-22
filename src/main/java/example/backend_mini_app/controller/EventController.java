package example.backend_mini_app.controller;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import example.backend_mini_app.config.EventConfig;
import example.backend_mini_app.config.event.EventConfigService;
import example.backend_mini_app.marker.PublicApi;
import example.backend_mini_app.service.EventDateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Event Controller", description = "Các API phục vụ xem các sự kiện, lễ hội")
@PublicApi
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventConfigService configService;
    private final EventDateService dateService;

    @Operation(
            summary = "Lấy danh sách cấu hình sự kiện",
            description = "Trả về toàn bộ danh sách cấu hình sự kiện từ file cấu hình."
    )
    @GetMapping("/config")
    public List<EventConfig> getConfigs() {
        return configService.getAll();
    }

    @Operation(
            summary = "Lấy danh sách sự kiện theo năm",
            description = "Trả về danh sách sự kiện đã được chuyển đổi sang ngày dương lịch trong năm chỉ định. Nếu không truyền năm, mặc định là năm hiện tại."
    )
    @GetMapping
    public List<EventDateService.EventView> listForYear(
            @RequestParam(required = false) Integer year) {
        int y = (year != null) ? year : LocalDate.now().getYear();
        return dateService.listForYear(y);
    }

    @Operation(
            summary = "Lấy sự kiện hôm nay",
            description = "Trả về danh sách sự kiện xảy ra vào ngày hiện tại."
    )
    @GetMapping("/today")
    public List<EventDateService.EventView> today() {
        return dateService.eventsOn(LocalDate.now());
    }

    @Operation(
            summary = "Lấy sự kiện theo ngày cụ thể",
            description = "Trả về danh sách sự kiện xảy ra vào ngày được chỉ định (định dạng yyyy-MM-dd)."
    )
    @GetMapping("/on")
    public List<EventDateService.EventView> eventsOn(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dateService.eventsOn(date);
    }

    @Operation(
            summary = "Lấy danh sách sự kiện sắp tới",
            description = "Trả về danh sách các sự kiện sắp tới tính từ hôm nay, giới hạn theo số lượng chỉ định (tối đa 100)."
    )
    @GetMapping("/next")
    public List<EventDateService.EventView> next(@RequestParam(defaultValue = "10") int limit) {
        return dateService.upcoming(Math.max(1, Math.min(limit, 100)));
    }

    @Operation(
            summary = "Chuyển đổi ngày âm sang ngày dương",
            description = "Chuyển đổi một ngày âm lịch sang ngày dương lịch. Nếu là tháng nhuận, truyền thêm tham số `leapMonth=true`."
    )
    @GetMapping("/convert/lunar-to-solar")
    public ResponseEntity<Solar> lunarToSolar(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam(defaultValue = "false") boolean leapMonth) {
        int lunarMonth = leapMonth ? -month : month;
        Solar solar = Lunar.fromYmd(year, lunarMonth, day).getSolar();
        return ResponseEntity.ok(solar);
    }

    @Operation(
            summary = "Chuyển đổi ngày dương sang ngày âm",
            description = "Chuyển đổi một ngày dương lịch sang ngày âm lịch tương ứng."
    )
    @GetMapping("/convert/solar-to-lunar")
    public ResponseEntity<Lunar> solarToLunar(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {
        Lunar lunar = Solar.fromYmd(year, month, day).getLunar();
        return ResponseEntity.ok(lunar);
    }
}