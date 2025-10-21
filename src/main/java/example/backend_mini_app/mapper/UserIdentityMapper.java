package example.backend_mini_app.mapper;

import example.backend_mini_app.config.MapStructConfig;
import example.backend_mini_app.model.ZaloProfile;
import example.backend_mini_app.model.entity.User;
import example.backend_mini_app.model.entity.UserIdentity;
import example.backend_mini_app.model.enumeration.Provider;
import example.backend_mini_app.model.response.UserIdentityResponse;
import org.mapstruct.*;

import java.time.Instant;

@Mapper(config = MapStructConfig.class)
public interface UserIdentityMapper {

    @Mapping(target = "provider", expression = "java(entity.getProvider().name())")
    @Mapping(target = "tokenExpiresAtEpoch",
            expression = "java(entity.getTokenExpiresAt()==null ? null : entity.getTokenExpiresAt().getEpochSecond())")
    UserIdentityResponse toResponse(UserIdentity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "provider", constant = "ZALO")
    @Mapping(target = "providerUserId", source = "profile.id")
    @Mapping(target = "providerUnionId", ignore = true)
    @Mapping(target = "accessToken",    ignore = true)
    @Mapping(target = "refreshToken",   ignore = true)
    @Mapping(target = "tokenScope",     ignore = true)
    @Mapping(target = "tokenExpiresAt", ignore = true)
    @Mapping(target = "rawProfileJson", source = "profile.rawJson")
    @Mapping(target = "firstLinkedAt",  expression = "java(java.time.Instant.now())")
    @Mapping(target = "lastLoginAt",    expression = "java(java.time.Instant.now())")
    UserIdentity newFromZalo(User user, ZaloProfile profile);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "rawProfileJson", source = "profile.rawJson"),
            @Mapping(target = "lastLoginAt",    expression = "java(java.time.Instant.now())")
    })
    void touchAfterLogin(ZaloProfile profile, @MappingTarget UserIdentity entity);

    default Provider providerOf(String s) { return s == null ? null : Provider.valueOf(s.toUpperCase()); }

    default void setTokens(UserIdentity entity, String accessToken, String refreshToken, String scope, Instant exp) {
        entity.setAccessToken(accessToken);
        entity.setRefreshToken(refreshToken);
        entity.setTokenScope(scope);
        entity.setTokenExpiresAt(exp);
    }
}


