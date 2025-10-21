package example.backend_mini_app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.backend_mini_app.config.property.ZaloOAuthProperties;
import example.backend_mini_app.exception.ErrorCode;
import example.backend_mini_app.exception.MiniAppException;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ZaloOAuthService {

    private final ZaloOAuthProperties props;
    private final OAuthStateRepository stateRepo;
    private final UserRepository userRepo;
    private final UserIdentityRepository uiRepo;
    private final UserMapper userMapper;
    private final UserIdentityMapper uiMapper;
    private final ObjectMapper objectMapper;

    private final @Qualifier("zaloWebClient") WebClient web;

    public ZaloOAuthService(ZaloOAuthProperties props, OAuthStateRepository stateRepo, UserRepository userRepo, UserIdentityRepository uiRepo, UserMapper userMapper, UserIdentityMapper uiMapper, ObjectMapper objectMapper, @Qualifier("zaloWebClient") WebClient web) {
        this.props = props;
        this.stateRepo = stateRepo;
        this.userRepo = userRepo;
        this.uiRepo = uiRepo;
        this.userMapper = userMapper;
        this.uiMapper = uiMapper;
        this.objectMapper = objectMapper;
        this.web = web;
    }

    @Transactional
    public AuthInitResponse init(AuthInitRequest req) {
        String redirectUri = Optional.ofNullable(req.getRedirectUri()).orElse(props.defaultRedirectUri());
        String stateStr = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(Optional.ofNullable(props.stateTtlSeconds()).orElse(600));

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
                .orElseThrow(() -> ex(ErrorCode.SYSTEM_INTERNAL_ERROR, "invalid_state"));
        if (st.getConsumedAt() != null) throw ex(ErrorCode.SYSTEM_INTERNAL_ERROR, "state_consumed");
        if (Instant.now().isAfter(st.getExpiresAt())) throw ex(ErrorCode.SYSTEM_INTERNAL_ERROR, "state_expired");
        if (!st.getRedirectUri().equals(req.getRedirectUri())) throw ex(ErrorCode.SYSTEM_INTERNAL_ERROR, "redirect_mismatch");
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

    private record TokenPayload(String accessToken, String refreshToken, long expiresInSeconds, String scope){}

    private TokenPayload exchangeCodeForToken(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("app_id", props.appId());
        form.add("app_secret", props.appSecret());

        String respBody;
        try {
            respBody = web.post()
                    .uri(props.tokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("secret_key", props.appSecret())
                    .bodyValue(form)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw ex(ErrorCode.ZALO_INVALID_RESPONSE,
                    "token_exchange_http_%d".formatted(ex.getRawStatusCode()), ex);
        } catch (WebClientRequestException ex) {
            throw ex(ErrorCode.ZALO_API_CONNECTION_ERROR, "token_exchange_connect_error", ex);
        } catch (Exception ex) {
            throw ex(ErrorCode.ZALO_INVALID_RESPONSE, "token_exchange_unknown_error", ex);
        }

        if (respBody == null || respBody.isBlank()) {
            throw ex(ErrorCode.ZALO_INVALID_RESPONSE, "token_exchange_empty");
        }

        try {
            JsonNode node = objectMapper.readTree(respBody);

            if (node.hasNonNull("error") || node.hasNonNull("error_code")) {
                String ec = node.path("error").asText(node.path("error_code").asText("unknown"));
                String msg = node.path("message").asText(node.path("error_description").asText(""));
                // map một số mã quen thuộc
                if ("invalid_grant".equalsIgnoreCase(ec) || "1004".equals(ec)) {
                    throw ex(ErrorCode.ZALO_AUTH_CODE_EXPIRED, "token_exchange_error: " + ec + " - " + msg);
                }
                throw ex(ErrorCode.ZALO_INVALID_RESPONSE, "token_exchange_error: " + ec + " - " + msg);
            }

            String at = textOrNull(node, "access_token");
            if (at == null) throw ex(ErrorCode.ZALO_INVALID_RESPONSE,
                    "token_exchange_no_access_token: " + truncate(respBody));

            String rt = textOrNull(node, "refresh_token");
            long exp = node.path("expires_in").asLong(3600);
            String scope = textOrNull(node, "scope");
            return new TokenPayload(at, rt, exp, scope);

        } catch (MiniAppException e) {
            throw e;
        } catch (Exception e) {
            throw ex(ErrorCode.ZALO_INVALID_RESPONSE, "token_exchange_parse_error: " + truncate(respBody), e);
        }
    }

    private ZaloProfile fetchProfile(String accessToken) {
        URI userinfo = URI.create(props.userinfoUrl());
        if (!userinfo.isAbsolute()) {
            throw ex(ErrorCode.ZALO_INVALID_RESPONSE, "userinfo_url_invalid: " + props.userinfoUrl());
        }

        String resp;
        try {
            resp = web.get()
                    .uri(userinfo)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            int sc = ex.getRawStatusCode();
            if (sc == 401 || sc == 403) {
                throw ex(ErrorCode.ZALO_ACCESS_TOKEN_INVALID, "userinfo_http_%d".formatted(sc), ex);
            }
            throw ex(ErrorCode.ZALO_INVALID_RESPONSE, "userinfo_http_%d".formatted(sc), ex);
        } catch (WebClientRequestException ex) {
            throw ex(ErrorCode.ZALO_API_CONNECTION_ERROR, "userinfo_connect_error", ex);
        } catch (Exception ex) {
            throw ex(ErrorCode.ZALO_INVALID_RESPONSE, "userinfo_unknown_error", ex);
        }

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
            if (p.getId() == null) throw ex(ErrorCode.ZALO_INVALID_RESPONSE, "userinfo_missing_id");
            return p;
        } catch (MiniAppException e) {
            throw e;
        } catch (Exception e) {
            throw ex(ErrorCode.ZALO_INVALID_RESPONSE, "userinfo_parse_error", e);
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

    private MiniAppException ex(ErrorCode code, String message) {
        return new MiniAppException(code, message);
    }

    private MiniAppException ex(ErrorCode code, String message, Throwable cause) {
        return new MiniAppException(code, message, cause);
    }

    private static String textOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private String truncate(String s) {
        return (s != null && s.length() > 500) ? s.substring(0, 500) + "...(truncated)" : s;
    }
}
