package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ArticleResponse {
    private Long id;
    private String slug;
    private String title;
    private String excerpt;
    private String bodyHtml;
    private String author;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private MediaResponse coverMedia;
    private List<TagResponse> tags;
    private String sourceName;
    private String sourceUrl;

}
