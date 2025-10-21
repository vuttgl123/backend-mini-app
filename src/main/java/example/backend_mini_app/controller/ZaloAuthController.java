package example.backend_mini_app.controller;

import example.backend_mini_app.marker.PublicApi;
import example.backend_mini_app.model.request.AuthInitRequest;
import example.backend_mini_app.model.request.ZaloCallbackRequest;
import example.backend_mini_app.model.response.AuthInitResponse;
import example.backend_mini_app.model.response.LoginResponse;
import example.backend_mini_app.service.ZaloOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@PublicApi
@RestController
@RequestMapping("/api/auth/zalo")
@RequiredArgsConstructor
public class ZaloAuthController {
    private final ZaloOAuthService service;

    @PostMapping("/init")
    public AuthInitResponse init(@Valid @RequestBody AuthInitRequest req) {
        return service.init(req);
    }

    @GetMapping("/init")
    public ResponseEntity<Void> initGet(HttpServletRequest http) {
        String host = opt(http.getHeader("X-Forwarded-Host"), http.getHeader("Host"));
        String scheme = opt(http.getHeader("X-Forwarded-Proto"), http.getScheme());
        String redirectUri = scheme + "://" + host + "/api/auth/zalo/callback";

        var req = new AuthInitRequest();
        req.setRedirectUri(redirectUri);
        var init = service.init(req);

        return ResponseEntity.status(302)
                .header("Location", init.getAuthorizationUrl())
                .build();
    }

    private static String opt(String a, String b) { return (a != null && !a.isBlank()) ? a : b; }

    @PostMapping("/callback")
    public LoginResponse callback(@Valid @RequestBody ZaloCallbackRequest req) {
        return service.callback(req);
    }

    @GetMapping("/callback")
    public ResponseEntity<LoginResponse> callbackGet(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletRequest http
    ) {
        String host = http.getHeader("X-Forwarded-Host");
        String proto = http.getHeader("X-Forwarded-Proto");
        String scheme = (proto != null) ? proto : http.getScheme();
        String authority = (host != null) ? host : http.getHeader("Host");
        String redirectUri = scheme + "://" + authority + http.getRequestURI();

        ZaloCallbackRequest body = new ZaloCallbackRequest();
        body.setCode(code);
        body.setState(state);
        body.setRedirectUri(redirectUri);

        return ResponseEntity.ok(service.callback(body));
    }
}