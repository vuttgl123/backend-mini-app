// example/backend_mini_app/config/WebClientConfig.java
package example.backend_mini_app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "zaloWebClient")
    public WebClient zaloWebClient(ObjectMapper objectMapper) {
        var decoder = new Jackson2JsonDecoder(objectMapper,
                MediaType.APPLICATION_JSON,
                MediaType.valueOf("application/*+json"),
                MediaType.valueOf("text/json"));
        var encoder = new Jackson2JsonEncoder(objectMapper,
                MediaType.APPLICATION_JSON,
                MediaType.valueOf("application/*+json"),
                MediaType.valueOf("text/json"));

        var strategies = ExchangeStrategies.builder().codecs(cfg -> {
            cfg.defaultCodecs().jackson2JsonDecoder(decoder);
            cfg.defaultCodecs().jackson2JsonEncoder(encoder);
        }).build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .defaultHeader("Accept", "application/json, text/json")
                .build();
    }
}
