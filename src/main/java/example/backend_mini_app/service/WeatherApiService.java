package example.backend_mini_app.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherApiService {
    @Value("${app.weather.key}") private String apiKey;
    @Value("${app.weather.ward.lat}") private double wardLat;
    @Value("${app.weather.ward.lon}") private double wardLon;

    private final WebClient weather;

    public WeatherApiService(@Qualifier("weatherWebClient") WebClient weather) {
        this.weather = weather;
    }

    public Mono<JsonNode> getCurrentVi() {
        return weather.get()
                .uri(uri -> uri.path("/current.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", wardLat + "," + wardLon)
                        .queryParam("lang", "vi")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    public Mono<JsonNode> getForecastVi(int days) {
        if (days < 1) days = 1;
        if (days > 7) days = 7;

        int finalDays = days;
        return weather.get()
                .uri(uri -> uri.path("/forecast.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", wardLat + "," + wardLon)
                        .queryParam("days", finalDays)
                        .queryParam("lang", "vi")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
}
