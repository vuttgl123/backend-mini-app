package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIdentityResponse {
    public String provider;
    public String providerUserId;
    public String providerUnionId;
    public String tokenScope;
    public Long tokenExpiresAtEpoch;
}
