package example.backend_mini_app.model.request;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {
    @NotBlank
    public String refreshToken;
}
