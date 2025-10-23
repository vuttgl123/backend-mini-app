package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagResponse {
    private Long id;
    private String name;
    private String slug;
}
