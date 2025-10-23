package example.backend_mini_app.mapper;


import example.backend_mini_app.config.MapStructConfig;
import example.backend_mini_app.model.entity.Media;
import example.backend_mini_app.model.response.MediaResponse;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface MediaMapper {

    @Mappings({
            @Mapping(target = "id",         source = "media.id"),
            @Mapping(target = "objectKey",  source = "media.objectKey"),
            @Mapping(target = "mimeType",   source = "media.mimeType"),
            @Mapping(target = "sizeBytes",  source = "media.sizeBytes"),
            @Mapping(target = "createdAt",  source = "media.createdAt"),
            @Mapping(target = "url",        ignore = true) // set ở @AfterMapping
    })
    MediaResponse toResponse(Media media, @Context String mediaBaseUrl);

    @AfterMapping
    default void fillUrl(@MappingTarget MediaResponse resp, Media media, @Context String mediaBaseUrl) {
        if (media != null && media.getObjectKey() != null && mediaBaseUrl != null) {
            resp.setUrl(mediaBaseUrl.endsWith("/")
                    ? mediaBaseUrl + media.getObjectKey()
                    : mediaBaseUrl + "/" + media.getObjectKey());
        }
    }
}
