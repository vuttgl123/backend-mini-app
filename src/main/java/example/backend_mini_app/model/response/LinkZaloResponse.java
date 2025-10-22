package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LinkZaloResponse {
    public List<UserIdentityResponse> identities;
}