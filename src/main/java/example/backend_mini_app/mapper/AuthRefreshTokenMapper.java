package example.backend_mini_app.mapper;

import example.backend_mini_app.config.MapStructConfig;
import example.backend_mini_app.model.entity.AuthRefreshToken;
import example.backend_mini_app.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(config = MapStructConfig.class)
public interface AuthRefreshTokenMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "tokenHash", source = "tokenHash")
    @Mapping(target = "issuedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "revokedAt", ignore = true)
    @Mapping(target = "ip", source = "ip")
    @Mapping(target = "userAgent", source = "userAgent")
    @Mapping(target = "replacedBy", ignore = true)
    AuthRefreshToken newToken(User user, String tokenHash, Instant expiresAt, String ip, String userAgent);
}
