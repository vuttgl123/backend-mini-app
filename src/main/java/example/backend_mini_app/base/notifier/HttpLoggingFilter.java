package example.backend_mini_app.base.notifier;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpLoggingFilter extends OncePerRequestFilter {
    private final Notifier notifier; private final NotifierProperties props;
    public HttpLoggingFilter(Notifier notifier, NotifierProperties props) {
        this.notifier = notifier; this.props = props;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(req, resp);
        } finally {
            long took = System.currentTimeMillis() - start;
            String reqBody = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);
            String respBody = props.isLogResponseBody() ? new String(resp.getContentAsByteArray(), StandardCharsets.UTF_8) : "";


            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("method", request.getMethod());
            meta.put("path", request.getRequestURI());
            meta.put("query", Truncator.clamp(request.getQueryString(), props.getMaxFieldChars()));
            meta.put("status", resp.getStatus());
            meta.put("tookMs", took);
            meta.put("reqHeaders", headersMap(request));
            meta.put("respHeaders", headersMap(resp));


            String msg = "REQ=" + Truncator.clamp(reqBody, props.getMaxBodyChars()) +
                    "\nRESP=" + Truncator.clamp(respBody, props.getMaxBodyChars());


            String level = resp.getStatus() >= 500 ? "ERROR" : (resp.getStatus() >= 400 ? "WARN" : "INFO");
            switch (level) {
                case "ERROR" -> notifier.error("http", "HTTP " + request.getMethod() + " " + request.getRequestURI(), msg, meta);
                case "WARN" -> notifier.warn("http", "HTTP " + request.getMethod() + " " + request.getRequestURI(), msg, meta);
                default -> notifier.info("http", "HTTP " + request.getMethod() + " " + request.getRequestURI(), msg, meta);
            }
            resp.copyBodyToResponse();
        }
    }


    private Map<String, Object> headersMap(HttpServletRequest request) {
        Map<String, Object> m = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) { String n = names.nextElement(); m.put(n, redact(n, request.getHeader(n))); }
        return m;
    }
    private Map<String, Object> headersMap(HttpServletResponse response) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (String n : response.getHeaderNames()) { m.put(n, redact(n, response.getHeader(n))); }
        return m;
    }
    private Object redact(String k, String v) { return props.getRedactKeys().contains(k.toLowerCase()) ? "[REDACTED]" : Truncator.clampValue(v, props.getMaxFieldChars()); }
}
