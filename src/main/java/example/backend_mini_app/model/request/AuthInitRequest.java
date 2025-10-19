package example.backend_mini_app.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthInitRequest {
    @NotBlank
    @Pattern(regexp = "^https?://.+")
    private String redirectUri;

    private String client;

}
