package example.backend_mini_app.mapper;

import example.backend_mini_app.config.MapStructConfig;
import example.backend_mini_app.model.entity.Tag;
import example.backend_mini_app.model.response.TagResponse;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface TagMapper {
    TagResponse toResponse(Tag tag);
}