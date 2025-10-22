package example.backend_mini_app.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutResponse {
    public boolean success;
    public String message;
}
