package example.backend_mini_app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Configuration
@EnableConfigurationProperties({WebClientConfig.HttpProps.class, WebClientConfig.ClientsProps.class})
public class WebClientConfig {

    @Data
    @ConfigurationProperties(prefix = "app.http")
    public static class HttpProps {
        private String userAgent = "MiniApp-BE/1.0";
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 10000;
        private int writeTimeoutMs = 10000;
        private int maxInMemorySizeMb = 10;
    }

    @Data
    @ConfigurationProperties(prefix = "app.clients")
    public static class ClientsProps {
        private Client zalo = new Client();
        private Client internalApi = new Client();
        private Client weather = new Client();

        @Data
        public static class Client {
            private String baseUrl;
            private String accept = "application/json";
        }
    }

    @Bean
    public ReactorClientHttpConnector reactorConnector(HttpProps http) {
        HttpClient client = HttpClient.create()
                .responseTimeout(Duration.ofMillis(http.getReadTimeoutMs()))
                .compress(true)
                .followRedirect(true)
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, http.getConnectTimeoutMs());
        return new ReactorClientHttpConnector(client);
    }

    @Primary
    @Bean
    public WebClient.Builder webClientBuilder(
            ReactorClientHttpConnector connector,
            ObjectMapper objectMapper,
            HttpProps http
    ) {
        MediaType[] jsonLikeArray = {
                MediaType.APPLICATION_JSON,
                MediaType.valueOf("application/*+json"),
                MediaType.valueOf("application/problem+json"),
                MediaType.valueOf("text/json"),
                MediaType.TEXT_PLAIN
        };

        Consumer<ClientCodecConfigurer> codecCustomizer = cfg -> {
            cfg.defaultCodecs().maxInMemorySize(http.getMaxInMemorySizeMb() * 1024 * 1024);
            cfg.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, jsonLikeArray));
            cfg.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, jsonLikeArray));
        };

        return WebClient.builder()
                .clientConnector(connector)
                .codecs(codecCustomizer)
                .defaultHeaders(h -> {
                    h.set("User-Agent", http.getUserAgent());
                    h.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.valueOf("text/json")));
                    h.setAcceptCharset(List.of(StandardCharsets.UTF_8));
                })
                .filter(logRequest())
                .filter(logResponse())
                .filter(mapErrors());
    }

    @Bean("zaloWebClient")
    public WebClient zaloWebClient(WebClient.Builder base, ClientsProps clients) {
        return base.clone()
                .baseUrl(clients.getZalo().getBaseUrl())
                .defaultHeader("Accept", clients.getZalo().getAccept())
                .build();
    }

    @Bean("internalApiWebClient")
    public WebClient internalApiWebClient(WebClient.Builder base, ClientsProps clients) {
        return base.clone()
                .baseUrl(clients.getInternalApi().getBaseUrl())
                .defaultHeader("Accept", clients.getInternalApi().getAccept())
                .build();
    }

    @Bean("weatherWebClient")
    public WebClient weatherWebClient(WebClient.Builder base, ClientsProps clients) {
        return base.clone()
                .baseUrl(clients.getWeather().getBaseUrl())
                .defaultHeader("Accept", clients.getWeather().getAccept())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.info("HTTP {} {}", req.method(), req.url());
            if (log.isDebugEnabled()) {
                req.headers().forEach((k, v) -> log.debug(">> {}: {}", k, v));
            }
            return Mono.just(req);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(resp -> {
            String ct = resp.headers().contentType()
                    .map(MediaType::toString)
                    .orElse("unknown");

            log.info("HTTP <{}> {}", resp.statusCode().value(), ct);
            return Mono.just(resp);
        });
    }

    private ExchangeFilterFunction mapErrors() {
        return (request, next) -> next.exchange(request)
                .flatMap(resp -> {
                    if (resp.statusCode().isError()) {
                        return resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(
                                        new RemoteApiException(resp.statusCode().value(), body)
                                ));
                    }
                    return Mono.just(resp);
                });
    }

    @Getter
    public static class RemoteApiException extends RuntimeException {
        private final int status;

        public RemoteApiException(int status, String body) {
            super("Remote API error: %d, body=%s".formatted(status, truncate(body)));
            this.status = status;
        }

        private static String truncate(String s) {
            if (s == null) return "";
            return s.length() > 2000 ? s.substring(0, 2000) + "..." : s;
        }
    }
}