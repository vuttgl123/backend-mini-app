package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthInitResponse {
    private String authorizationUrl;
    private String state;
    private Long stateExpiresAtEpoch;
}
