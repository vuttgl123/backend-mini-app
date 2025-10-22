package example.backend_mini_app.controller;

import example.backend_mini_app.marker.PublicApi;
import example.backend_mini_app.model.request.AuthInitRequest;
import example.backend_mini_app.model.request.ZaloCallbackRequest;
import example.backend_mini_app.model.response.AuthInitResponse;
import example.backend_mini_app.model.response.LoginResponse;
import example.backend_mini_app.service.ZaloOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Zalo Authentication", description = "Các API phục vụ đăng nhập qua Zalo")
@PublicApi
@RestController
@RequestMapping("/api/auth/zalo")
@RequiredArgsConstructor
public class ZaloAuthController {
    private final ZaloOAuthService service;

    @Operation(
            summary = "Khởi tạo luồng đăng nhập Zalo (POST)",
            description = "Tạo URL xác thực Zalo dựa trên redirectUri được cung cấp trong request body."
    )
    @PostMapping("/init")
    public AuthInitResponse init(@Valid @RequestBody AuthInitRequest req) {
        return service.init(req);
    }

    @Operation(
            summary = "Khởi tạo luồng đăng nhập Zalo (GET)",
            description = "Tạo URL xác thực Zalo từ thông tin header và redirect về trang xác thực."
    )
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

    private static String opt(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    @Operation(
            summary = "Xử lý callback từ Zalo (POST)",
            description = "Nhận mã xác thực từ Zalo và thực hiện đăng nhập bằng thông tin trong request body."
    )
    @PostMapping("/callback")
    public LoginResponse callback(@Valid @RequestBody ZaloCallbackRequest req) {
        return service.callback(req);
    }

    @Operation(
            summary = "Xử lý callback từ Zalo (GET)",
            description = "Nhận mã xác thực từ Zalo qua query param và thực hiện đăng nhập."
    )
    @GetMapping("/callback")
    public ResponseEntity<LoginResponse> callbackGet(
            @Parameter(description = "Mã xác thực từ Zalo") @RequestParam("code") String code,
            @Parameter(description = "State để xác thực phiên đăng nhập") @RequestParam("state") String state,
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