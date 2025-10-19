package example.backend_mini_app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "docs")
public record DocsProperties(List<Group> groups) {
    public enum Marker { publicApi, internalApi, none }
    public record Group(
            String name,
            String description,
            boolean enabled,
            boolean requireJwt,
            Marker marker,
            List<String> packages,
            List<String> includePaths,
            List<String> excludePaths,
            List<String> servers
    ) {}
}
