package example.backend_mini_app.model.request;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequest {
    @NotBlank
    public String refreshToken;
    public String fingerprint;
}
