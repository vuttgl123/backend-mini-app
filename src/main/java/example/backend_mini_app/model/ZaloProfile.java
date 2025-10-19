package example.backend_mini_app.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZaloProfile {
    private String id;
    private String name;
    private String picture;
    private String phone;
    private String email;
    private String rawJson;
}
