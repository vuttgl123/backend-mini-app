package example.backend_mini_app.model.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ArticleRequest {
    private String title;
    private String slug;
    private String excerpt;
    private String bodyMd;
    private Long coverMediaId;
    private List<String> tags;
    private String sourceName;
    private String sourceUrl;

}