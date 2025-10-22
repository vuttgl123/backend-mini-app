package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPublicResponse {
    public Long id;
    public String displayName;
    public String avatarUrl;
    public String email;
    public String phone;
    public String status;
}
