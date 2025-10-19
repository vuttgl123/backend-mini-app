package example.backend_mini_app.model.request;

import jakarta.validation.constraints.NotBlank;

public class LinkZaloCallbackRequest {
    @NotBlank public String code;
    @NotBlank public String state;
    @NotBlank public String redirectUri;
}
