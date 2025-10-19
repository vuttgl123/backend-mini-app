
package example.backend_mini_app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.backend_mini_app.config.ZaloOAuthProperties;
import example.backend_mini_app.mapper.UserIdentityMapper;
import example.backend_mini_app.mapper.UserMapper;
import example.backend_mini_app.model.ZaloProfile;
import example.backend_mini_app.model.entity.OAuthState;
import example.backend_mini_app.model.entity.User;
import example.backend_mini_app.model.enumeration.Provider;
import example.backend_mini_app.model.request.AuthInitRequest;
import example.backend_mini_app.model.request.ZaloCallbackRequest;
import example.backend_mini_app.model.response.AuthInitResponse;
import example.backend_mini_app.model.response.AuthTokenPairResponse;
import example.backend_mini_app.model.response.LoginResponse;
import example.backend_mini_app.repository.OAuthStateRepository;
import example.backend_mini_app.repository.UserIdentityRepository;
import example.backend_mini_app.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZaloOAuthService {

    private final ZaloOAuthProperties props;
    private final OAuthStateRepository stateRepo;
    private final UserRepository userRepo;
    private final UserIdentityRepository uiRepo;
    private final UserMapper userMapper;
    private final UserIdentityMapper uiMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final @Qualifier("zaloWebClient") WebClient web;

    @Transactional
    public AuthInitResponse init(AuthInitRequest req) {
        String redirectUri = (req.getRedirectUri() != null) ? req.getRedirectUri() : props.defaultRedirectUri();
        String stateStr = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.stateTtlSeconds() != null ? props.stateTtlSeconds() : 600);

        var st = new OAuthState();
        st.setProvider(Provider.ZALO);
        st.setState(stateStr);
        st.setRedirectUri(redirectUri);
        st.setCreatedAt(now);
        st.setExpiresAt(exp);
        stateRepo.save(st);

        URI authorize = UriComponentsBuilder.fromUriString(props.authUrl())
                .queryParam("response_type", "code")
                .queryParam("app_id", props.appId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", stateStr)
                .queryParam("scope", props.scope())
                .build(true).toUri();

        var resp = new AuthInitResponse();
        resp.setAuthorizationUrl(authorize.toString());
        resp.setState(stateStr);
        resp.setStateExpiresAtEpoch(exp.getEpochSecond());
        return resp;
    }

    @Transactional
    public LoginResponse callback(ZaloCallbackRequest req) {
        var st = stateRepo.findByState(req.getState())
                .orElseThrow(() -> new IllegalArgumentException("invalid_state"));
        if (st.getConsumedAt() != null) throw new IllegalStateException("state_consumed");
        if (Instant.now().isAfter(st.getExpiresAt())) throw new IllegalStateException("state_expired");
        if (!st.getRedirectUri().equals(req.getRedirectUri())) throw new IllegalStateException("redirect_mismatch");
        st.setConsumedAt(Instant.now());

        var token = exchangeCodeForToken(req.getCode(), req.getRedirectUri());
        var profile = fetchProfile(token.accessToken());
        var user = upsertFromZalo(profile, token);

        var pair = new AuthTokenPairResponse();
        pair.accessToken = "YOUR_APP_JWT";
        pair.refreshToken = "YOUR_APP_REFRESH";
        pair.expiresInSeconds = 3600;

        var resp = new LoginResponse();
        resp.user = userMapper.toPublic(user);
        resp.identities = user.getIdentities().stream().map(uiMapper::toResponse).toList();
        resp.tokens = pair;
        return resp;
    }

    /* ---------------- Helpers ---------------- */

    private record TokenPayload(String accessToken, String refreshToken, long expiresInSeconds, String scope){}

    private TokenPayload exchangeCodeForToken(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("app_id", props.appId());
        form.add("app_secret", props.appSecret()); // không hại, cứ gửi

        String resp = web.post()
                .uri(props.tokenUrl()) // https://oauth.zaloapp.com/v4/access_token
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("secret_key", props.appSecret()) // Quan trọng nếu toggle đang bật
                .bodyValue(form)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (resp == null || resp.isBlank()) throw new IllegalStateException("token_exchange_empty");

        try {
            var node = objectMapper.readTree(resp);

            if (node.has("error") || node.has("error_code")) {
                String ec = node.path("error").asText(node.path("error_code").asText("unknown"));
                String msg = node.path("message").asText(node.path("error_description").asText(""));
                throw new IllegalStateException("token_exchange_error: " + ec + " - " + msg);
            }

            String at = node.path("access_token").asText(null);
            if (at == null) throw new IllegalStateException("token_exchange_no_access_token: " + truncate(resp));

            String rt = node.path("refresh_token").asText(null);
            long exp = node.path("expires_in").asLong(3600);
            String scope = node.path("scope").asText(null);
            return new TokenPayload(at, rt, exp, scope);

        } catch (Exception e) {
            throw new IllegalStateException("token_exchange_parse_error: " + truncate(resp));
        }
    }

    private ZaloProfile fetchProfile(String accessToken) {
        URI userinfo = URI.create(props.userinfoUrl());
        if (!userinfo.isAbsolute()) {
            throw new IllegalStateException("userinfo_url_invalid: " + props.userinfoUrl());
        }
        String resp = web.get()
                .uri(userinfo)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            Map body = objectMapper.readValue(resp, Map.class);
            var p = new ZaloProfile();
            if (body != null) {
                p.setId(String.valueOf(body.get("id")));
                p.setName((String) body.getOrDefault("name", null));
                Object pic = body.get("picture");
                if (pic == null) pic = body.get("avatar");
                p.setPicture(pic == null ? null : String.valueOf(pic));
                p.setPhone((String) body.getOrDefault("phone", null));
                p.setEmail((String) body.getOrDefault("email", null));
                p.setRawJson(safeJson(body));
            }
            if (p.getId() == null) throw new IllegalStateException("userinfo_missing_id");
            return p;
        } catch (Exception e) {
            throw new IllegalStateException("userinfo_parse_error");
        }
    }

    private String safeJson(Map<String, Object> body) {
        try { return objectMapper.writeValueAsString(body); }
        catch (Exception e) { return "{}"; }
    }

    private User upsertFromZalo(ZaloProfile profile, TokenPayload token) {
        var opt = uiRepo.findByProviderAndProviderUserId(Provider.ZALO, profile.getId());
        if (opt.isPresent()) {
            var iden = opt.get();
            userMapper.patchFromZalo(profile, iden.getUser());
            uiMapper.touchAfterLogin(profile, iden);
            iden.setAccessToken(token.accessToken());
            iden.setTokenScope(token.scope());
            iden.setTokenExpiresAt(Instant.now().plusSeconds(token.expiresInSeconds()));
            return iden.getUser();
        } else {
            var user = userMapper.newFromZalo(profile);
            var iden = uiMapper.newFromZalo(user, profile);
            iden.setAccessToken(token.accessToken());
            iden.setRefreshToken(token.refreshToken());
            iden.setTokenScope(token.scope());
            iden.setTokenExpiresAt(Instant.now().plusSeconds(token.expiresInSeconds()));
            user.addIdentity(iden);
            return userRepo.save(user);
        }
    }

    private String truncate(String s) { return s != null && s.length() > 500 ? s.substring(0,500)+"...(truncated)" : s; }
}
