package example.backend_mini_app.model.request;

import jakarta.validation.constraints.NotBlank;

public class UnlinkProviderRequest {
    @NotBlank public String provider;
    @NotBlank
    public String providerUserId;
}
