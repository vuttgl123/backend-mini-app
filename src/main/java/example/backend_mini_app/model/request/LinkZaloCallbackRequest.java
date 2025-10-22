package example.backend_mini_app.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkZaloCallbackRequest {
    @NotBlank public String code;
    @NotBlank public String state;
    @NotBlank public String redirectUri;
}
