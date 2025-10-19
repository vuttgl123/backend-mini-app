package example.backend_mini_app.mapper;

import example.backend_mini_app.model.ZaloProfile;
import example.backend_mini_app.model.entity.User;
import example.backend_mini_app.model.enumeration.UserStatus;
import example.backend_mini_app.model.response.UserPublicResponse;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    UserPublicResponse toPublic(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phone",       source = "phone")
    @Mapping(target = "displayName", source = "name")
    @Mapping(target = "avatarUrl",   source = "picture")
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "identities",  ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    User newFromZalo(ZaloProfile profile);

    @BeanMapping(ignoreByDefault = true) // tránh lỗi unmapped target
    @Mappings({
            @Mapping(target = "displayName", source = "name"),
            @Mapping(target = "avatarUrl",   source = "picture"),
            @Mapping(target = "phone",       source = "phone")
    })
    void patchFromZalo(ZaloProfile profile, @MappingTarget User user);

    default UserStatus toStatus(String s) {
        if (s == null) return null;
        try { return UserStatus.valueOf(s.toUpperCase()); } catch (Exception e) { return null; }
    }
}
