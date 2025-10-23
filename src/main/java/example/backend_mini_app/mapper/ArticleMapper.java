package example.backend_mini_app.mapper;

import example.backend_mini_app.config.MapStructConfig;
import example.backend_mini_app.model.entity.Article;
import example.backend_mini_app.model.entity.Media;
import example.backend_mini_app.model.entity.Tag;
import example.backend_mini_app.model.enumeration.ArticleStatus;
import example.backend_mini_app.model.request.ArticleRequest;
import example.backend_mini_app.model.response.ArticleResponse;
import example.backend_mini_app.model.response.TagResponse;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mapper(config = MapStructConfig.class, uses = { MediaMapper.class, TagMapper.class })
public interface ArticleMapper {
    @Mappings({
            @Mapping(target = "id",          source = "article.id"),
            @Mapping(target = "slug",        source = "article.slug"),
            @Mapping(target = "title",       source = "article.title"),
            @Mapping(target = "excerpt",     source = "article.excerpt"),
            @Mapping(target = "bodyHtml",    source = "article.bodyHtml"),
            @Mapping(target = "author",      source = "article.author"),
            @Mapping(target = "status",      expression = "java(article.getStatus() != null ? article.getStatus().name() : null)"),
            @Mapping(target = "publishedAt", source = "article.publishedAt"),
            @Mapping(target = "createdAt",   source = "article.createdAt"),
            @Mapping(target = "updatedAt",   source = "article.updatedAt"),
            @Mapping(target = "sourceName",  source = "article.sourceName"),
            @Mapping(target = "sourceUrl",   source = "article.sourceUrl"),

            @Mapping(target = "coverMedia",  expression = "java(mediaMapper.toResponse(article.getCoverMedia(), mediaBaseUrl))"),
            @Mapping(target = "tags",        expression = "java(tagMapperList(article))")
    })
    ArticleResponse toResponse(Article article,
                               @Context String mediaBaseUrl,
                               @Context MediaMapper mediaMapper,
                               @Context TagMapper tagMapper);


    default List<TagResponse> tagMapperList(Article a) {
        if (a == null || a.getTags() == null) return List.of();
        return a.getTags().stream()
                .map(t -> {
                    TagResponse tr = new TagResponse();
                    tr.setId(t.getId());
                    tr.setName(t.getName());
                    tr.setSlug(t.getSlug());
                    return tr;
                })
                .toList();
    }


    @Mappings({
            @Mapping(target = "id",           ignore = true),
            @Mapping(target = "title",        source = "req.title"),
            @Mapping(target = "slug",         source = "req.slug"),
            @Mapping(target = "excerpt",      source = "req.excerpt"),
            @Mapping(target = "bodyMd",       source = "req.bodyMd"),
            @Mapping(target = "bodyHtml",     ignore = true),
            @Mapping(target = "status",       constant = "DRAFT"),
            @Mapping(target = "author",       source = "author"),
            @Mapping(target = "coverMedia",   source = "cover"),
            @Mapping(target = "sourceName",   source = "req.sourceName"),
            @Mapping(target = "sourceUrl",    source = "req.sourceUrl"),
            @Mapping(target = "publishedAt",  ignore = true),
            @Mapping(target = "createdAt",    ignore = true),
            @Mapping(target = "updatedAt",    ignore = true),
            @Mapping(target = "tags",         source = "tags")
    })
    Article toEntity(ArticleRequest req, Media cover, Set<Tag> tags, String author);

    @AfterMapping
    default void fillTimestamps(@MappingTarget Article a) {
        LocalDateTime now = LocalDateTime.now();
        if (a.getCreatedAt() == null) a.setCreatedAt(now);
        if (a.getUpdatedAt() == null) a.setUpdatedAt(now);
        if (a.getStatus() == ArticleStatus.PUBLISHED && a.getPublishedAt() == null) {
            a.setPublishedAt(now);
        }
    }


    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "title",      source = "req.title"),
            @Mapping(target = "excerpt",    source = "req.excerpt"),
            @Mapping(target = "bodyMd",     source = "req.bodyMd"),
            @Mapping(target = "sourceName", source = "req.sourceName"),
            @Mapping(target = "sourceUrl",  source = "req.sourceUrl"),
            @Mapping(target = "coverMedia", source = "cover"),
            @Mapping(target = "tags",       source = "tags")
    })
    void patch(@MappingTarget Article article,
               ArticleRequest req,
               Media cover,
               Set<Tag> tags);

    @AfterMapping
    default void touchUpdatedAt(@MappingTarget Article a) {
        a.setUpdatedAt(LocalDateTime.now());
    }


    default Set<Tag> toTagSet(List<Tag> list) {
        if (list == null) return null;
        return new LinkedHashSet<>(list);
    }
}
