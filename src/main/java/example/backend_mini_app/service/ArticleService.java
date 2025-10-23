package example.backend_mini_app.service;

import example.backend_mini_app.mapper.ArticleMapper;
import example.backend_mini_app.model.entity.Article;
import example.backend_mini_app.model.entity.Media;
import example.backend_mini_app.model.entity.Tag;
import example.backend_mini_app.model.enumeration.ArticleStatus;
import example.backend_mini_app.model.request.ArticleRequest;
import example.backend_mini_app.model.response.ArticleResponse;
import example.backend_mini_app.repository.ArticleRepo;
import example.backend_mini_app.repository.MediaRepo;
import example.backend_mini_app.repository.TagRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepo articleRepo;
    private final MediaRepo mediaRepo;
    private final TagRepo tagRepo;

    private final ArticleMapper articleMapper;
    private final ContentRenderService contentRenderService;

    @Value("${app.cdn.baseUrl}")
    private final String mediaBaseUrl = "http://127.0.0.1:9000/mybucket";


    @Transactional(readOnly = true)
    public ArticleResponse getPublicBySlug(String slug) {
        Article a = articleRepo.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED).orElseThrow(() -> notFound("Article", "slug", slug));
        return articleMapper.toResponse(a, mediaBaseUrl, null, null);
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> listPublished(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Article> p;
        if (q == null || q.isBlank()) {
            p = articleRepo.findAll(Example.of(new Article(){{
                setStatus(ArticleStatus.PUBLISHED);
            }}, ExampleMatcher.matchingAny()), pageable);
        } else {
            p = articleRepo.findByStatusAndTitleContainingIgnoreCaseOrStatusAndExcerptContainingIgnoreCase(
                    ArticleStatus.PUBLISHED, q, ArticleStatus.PUBLISHED, q, pageable
            );
        }
        return p.map(a -> articleMapper.toResponse(a, mediaBaseUrl, null, null));
    }


    @Transactional
    public ArticleResponse create(ArticleRequest req, String author, boolean autoCreateTags) {
        Media cover = resolveCover(req.getCoverMediaId());
        Set<Tag> tags = resolveTags(req.getTags(), autoCreateTags);
        Article a = articleMapper.toEntity(req, cover, tags, author);
        a = articleRepo.save(a);
        return articleMapper.toResponse(a, mediaBaseUrl, null, null);
    }

    @Transactional
    public ArticleResponse patch(Long id, ArticleRequest req, boolean autoCreateTags) {
        Article a = articleRepo.findById(id).orElseThrow(() -> notFound("Article", "id", id));
        Media cover = resolveCover(req.getCoverMediaId());
        Set<Tag> tags = resolveTags(req.getTags(), autoCreateTags);
        articleMapper.patch(a, req, cover, tags);
        a = articleRepo.save(a);
        return articleMapper.toResponse(a, mediaBaseUrl, null, null);
    }

    @Transactional
    public ArticleResponse publish(Long id) {
        Article a = articleRepo.findById(id).orElseThrow(() -> notFound("Article", "id", id));
        String safeHtml = contentRenderService.renderAndSanitize(a.getBodyMd());
        a.setBodyHtml(safeHtml);
        a.setStatus(ArticleStatus.PUBLISHED);
        if (a.getPublishedAt() == null) {
            a.setPublishedAt(java.time.LocalDateTime.now());
        }
        a = articleRepo.save(a);
        return articleMapper.toResponse(a, mediaBaseUrl, null, null);
    }

    @Transactional
    public ArticleResponse changeStatus(Long id, ArticleStatus status) {
        Article a = articleRepo.findById(id).orElseThrow(() -> notFound("Article", "id", id));
        if (status == ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("Use publish() to change status to PUBLISHED.");
        }
        a.setStatus(status);
        a = articleRepo.save(a);
        return articleMapper.toResponse(a, mediaBaseUrl, null, null);
    }

    @Transactional(readOnly = true)
    public ArticleResponse getAdminById(Long id) {
        Article a = articleRepo.findById(id).orElseThrow(() -> notFound("Article", "id", id));
        return articleMapper.toResponse(a, mediaBaseUrl, null, null);
    }

    @Transactional
    public void deleteHard(Long id) {
        articleRepo.deleteById(id);
    }


    private Media resolveCover(Long coverMediaId) {
        if (coverMediaId == null) return null;
        return mediaRepo.findById(coverMediaId)
                .orElseThrow(() -> notFound("Media", "id", coverMediaId));
    }

    private Set<Tag> resolveTags(List<String> tagSlugs, boolean autoCreate) {
        if (tagSlugs == null || tagSlugs.isEmpty()) return new LinkedHashSet<>();
        List<Tag> found = tagRepo.findAll().stream()
                .filter(t -> tagSlugs.contains(t.getSlug()))
                .collect(Collectors.toList());

        Map<String, Tag> map = found.stream().collect(Collectors.toMap(Tag::getSlug, t -> t));

        if (autoCreate) {
            for (String slug : tagSlugs) {
                if (!map.containsKey(slug)) {
                    Tag t = new Tag();
                    t.setSlug(slug);
                    t.setName(slug.replace('-', ' '));
                    tagRepo.save(t);
                    map.put(slug, t);
                }
            }
        }
        return tagSlugs.stream().map(map::get).filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private NoSuchElementException notFound(String type, String field, Object value) {
        return new NoSuchElementException(type + " not found by " + field + "=" + value);
    }
}

