package example.backend_mini_app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import example.backend_mini_app.marker.PublicApi;
import example.backend_mini_app.service.WeatherApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Weather Controller", description = "Các API phục vụ xem thời tiết")
@PublicApi
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherApiService weather;

    @Operation(
            summary = "Lấy thông tin thời tiết hiện tại",
            description = "Trả về dữ liệu thời tiết hiện tại tại phường Hải Dương, thành phố Hải Phòng"
    )
    @GetMapping("/current")
    public Mono<ResponseEntity<JsonNode>> getCurrentWeather() {
        return weather.getCurrentVi().map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Dự báo thời tiết",
            description = "Trả về dự báo thời tiết trong số ngày được chỉ định (mặc định là 3 ngày) tại phường Hải Dương, thành phố Hải Phòng."
    )
    @GetMapping("/forecast")
    public Mono<ResponseEntity<JsonNode>> forecast(
            @Parameter(description = "Số ngày dự báo (tối đa theo giới hạn của API)")
            @RequestParam(defaultValue = "3") int days
    ) {
        return weather.getForecastVi(days).map(ResponseEntity::ok);
    }
}
