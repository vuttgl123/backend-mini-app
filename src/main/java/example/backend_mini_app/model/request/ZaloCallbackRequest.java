package example.backend_mini_app.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZaloCallbackRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String state;

    @NotBlank
    private String redirectUri;
}