package example.backend_mini_app.model.response;

import java.util.List;

public class LoginResponse {
    public UserPublicResponse user;
    public List<UserIdentityResponse> identities;
    public AuthTokenPairResponse tokens;
}