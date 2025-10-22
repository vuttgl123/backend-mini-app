package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkZaloInitResponse {
    public String authorizationUrl;
    public String state;
    public Long   stateExpiresAtEpoch;
}
