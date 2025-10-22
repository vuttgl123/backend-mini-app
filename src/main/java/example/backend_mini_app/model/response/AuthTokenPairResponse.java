package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthTokenPairResponse {
    public String accessToken;
    public String refreshToken;
    public long   expiresInSeconds;
}
