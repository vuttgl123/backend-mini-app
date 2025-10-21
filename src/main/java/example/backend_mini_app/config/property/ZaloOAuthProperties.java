package example.backend_mini_app.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zalo.oauth")
public record ZaloOAuthProperties(
        String appId,
        String appSecret,
        String defaultRedirectUri,
        String scope,
        String authUrl,
        String tokenUrl,
        String userinfoUrl,
        Integer stateTtlSeconds
) {}
