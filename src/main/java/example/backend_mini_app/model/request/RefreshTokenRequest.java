package example.backend_mini_app.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    @NotBlank
    public String refreshToken;
    public String fingerprint;
}
