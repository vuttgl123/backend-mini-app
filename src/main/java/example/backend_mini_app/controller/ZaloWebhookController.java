package example.backend_mini_app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/zalo")
public class ZaloWebhookController {

    @Value("${app.webhook.secret:}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handle(@RequestHeader(value = "X-ZMA-Signature", required = false) String signature,
                                       @RequestBody String body) {
        if (!webhookSecret.isBlank() && signature != null && !verify(body, webhookSecret, signature)) {
            log.warn("Invalid webhook signature");
            return ResponseEntity.status(401).build();
        }
        log.info("Zalo webhook payload: {}", body);
        return ResponseEntity.ok().build();
    }

    private boolean verify(String payload, String secret, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = Base64.getEncoder().encodeToString(digest);
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Webhook verify error", e);
            return false;
        }
    }

    @GetMapping("/webhook/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}

