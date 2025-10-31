package example.backend_mini_app.base.notifier;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ServiceWithNotifier {
    private final Notifier notifier;
    public ServiceWithNotifier(Notifier notifier) { this.notifier = notifier; }


    public void processOrder(String orderId, String payload) {
        notifier.info("order", "process start", payload, Map.of("orderId", orderId));
        try {
// do work ...
            String result = "ok:" + orderId;
            notifier.info("order", "process done", result, Map.of("orderId", orderId));
        } catch (Exception e) {
            notifier.error("order", "process failed", e.getMessage(), Map.of("orderId", orderId));
            throw e;
        }
        // ===================== HOW TO USE (notifier) =====================
// 1) Để Spring Boot scan package `com.example.core.notifier` (hoặc `@Import(NotifierAutoConfig.class)`).
// 2) application.yml ví dụ:
// core:
// notifier:
// max-body-chars: 3000
// max-field-chars: 200
// sample-rate: 0.5
// log-response-body: true
// redact-keys: [authorization, cookie, password, token]
// 3) HTTP sẽ tự log REQ/RESP qua HttpLoggingFilter (clamp + redact). Trong code: inject `Notifier` và gọi info/warn/error.

    }
}
