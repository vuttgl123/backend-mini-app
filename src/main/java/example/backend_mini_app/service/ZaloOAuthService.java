package example.backend_mini_app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.backend_mini_app.config.jwt.JwtService;
import example.backend_mini_app.config.property.JwtProperties;
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
import example.backend_mini_app.shared.helper.ErrorHelper;
import example.backend_mini_app.shared.helper.WebClientHelper;
import example.backend_mini_app.shared.util.JsonUtils;
import example.backend_mini_app.shared.util.StringUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ZaloOAuthService {

    private final ZaloOAuthProperties props;
    private final JwtProperties jwtProps;
    private final OAuthStateRepository stateRepo;
    private final UserRepository userRepo;
    private final UserIdentityRepository uiRepo;
    private final UserMapper userMapper;
    private final UserIdentityMapper uiMapper;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final WebClient web;

    public ZaloOAuthService(ZaloOAuthProperties props,
                            JwtProperties jwtProps,
                            OAuthStateRepository stateRepo,
                            UserRepository userRepo,
                            UserIdentityRepository uiRepo,
                            UserMapper userMapper,
                            UserIdentityMapper uiMapper,
                            ObjectMapper objectMapper,
                            @Qualifier("zaloWebClient") WebClient web,
                            JwtService jwtService) {
        this.props = props;
        this.jwtProps = jwtProps;
        this.stateRepo = stateRepo;
        this.userRepo = userRepo;
        this.uiRepo = uiRepo;
        this.userMapper = userMapper;
        this.uiMapper = uiMapper;
        this.objectMapper = objectMapper;
        this.web = web;
        this.jwtService = jwtService;
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
                .orElseThrow(() -> ErrorHelper.ex(ErrorCode.SYSTEM_INTERNAL_ERROR, "invalid_state"));
        if (st.getConsumedAt() != null) throw ErrorHelper.ex(ErrorCode.STATE_ALREADY_CONSUMED, "state_consumed");
        if (Instant.now().isAfter(st.getExpiresAt())) throw ErrorHelper.ex(ErrorCode.STATE_EXPIRED, "state_expired");
        if (!st.getRedirectUri().equals(req.getRedirectUri())) throw ErrorHelper.ex(ErrorCode.STATE_REDIRECT_URI_MISMATCH, "redirect_mismatch");
        st.setConsumedAt(Instant.now());

        var token = exchangeCodeForToken(req.getCode(), req.getRedirectUri());
        var profile = fetchProfile(token.accessToken());
        var user = upsertFromZalo(profile, token);

        String username = determineUsername(user);
        List<String> roles = List.of("USER");

        String accessToken = jwtService.generateAccessToken(username, roles);
        String refreshToken = jwtService.generateRefreshToken(username);

        var pair = new AuthTokenPairResponse();
        pair.accessToken = accessToken;
        pair.refreshToken = refreshToken;
        pair.expiresInSeconds = jwtProps.accessTokenTtlMinutes() * 60;

        var resp = new LoginResponse();
        resp.user = userMapper.toPublic(user);
        resp.identities = user.getIdentities().stream().map(uiMapper::toResponse).toList();
        resp.tokens = pair;
        return resp;
    }

    private String determineUsername(User user) {
        if (!StringUtils.isBlank(user.getEmail())) return user.getEmail();
        if (!StringUtils.isBlank(user.getPhone())) return user.getPhone();
        return "user_" + user.getId();
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
        } catch (WebClientResponseException | WebClientRequestException e) {
            throw WebClientHelper.mapException(e, "token_exchange");
        } catch (Exception e) {
            throw ErrorHelper.ex(ErrorCode.REMOTE_INVALID_RESPONSE, "token_exchange_unknown_error", e);
        }

        if (respBody == null || respBody.isBlank()) {
            throw ErrorHelper.ex(ErrorCode.REMOTE_INVALID_RESPONSE, "token_exchange_empty");
        }

        try {
            JsonNode node = objectMapper.readTree(respBody);

            if (JsonUtils.hasAny(node, "error", "error_code")) {
                String ec = node.path("error").asText(node.path("error_code").asText("unknown"));
                String msg = node.path("message").asText(node.path("error_description").asText(""));
                if ("invalid_grant".equalsIgnoreCase(ec) || "1004".equals(ec)) {
                    throw ErrorHelper.ex(ErrorCode.AUTH_CODE_EXPIRED, "token_exchange_error: " + ec + " - " + msg);
                }
                throw ErrorHelper.ex(ErrorCode.REMOTE_INVALID_RESPONSE, "token_exchange_error: " + ec + " - " + msg);
            }

            String at = JsonUtils.getText(node, "access_token");
            if (at == null) {
                throw ErrorHelper.ex(ErrorCode.REMOTE_INVALID_RESPONSE, "token_exchange_no_access_token: " + StringUtils.truncate(respBody, 500)
                );
            }

            String rt = JsonUtils.getText(node, "refresh_token");
            long exp = node.path("expires_in").asLong(3600);
            String scope = JsonUtils.getText(node, "scope");
            return new TokenPayload(at, rt, exp, scope);

        } catch (MiniAppException e) {
            throw e;
        } catch (Exception e) {
            throw ErrorHelper.ex(ErrorCode.REMOTE_INVALID_RESPONSE, "token_exchange_parse_error: " + StringUtils.truncate(respBody, 500), e);
        }
    }

    private ZaloProfile fetchProfile(String accessToken) {
        URI userinfo = URI.create(props.userinfoUrl());
        if (!userinfo.isAbsolute()) {
            throw ErrorHelper.ex(ErrorCode.SYSTEM_INTERNAL_ERROR, "userinfo_url_invalid: " + props.userinfoUrl());
        }

        String resp;
        try {
            resp = web.get()
                    .uri(userinfo)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException e) {
            if (e instanceof WebClientResponseException wre) {
                int sc = wre.getStatusCode().value();
                if (sc == 401) {
                    throw ErrorHelper.ex(ErrorCode.AUTH_ACCESS_TOKEN_INVALID, "userinfo_http_401", e);
                }
                if (sc == 403) {
                    throw ErrorHelper.ex(ErrorCode.AUTH_USER_DENIED, "userinfo_http_403", e);
                }
            }
            throw WebClientHelper.mapException(e, "userinfo");
        } catch (Exception e) {
            throw ErrorHelper.ex(ErrorCode.REMOTE_INVALID_RESPONSE, "userinfo_unknown_error", e);
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
                p.setRawJson(JsonUtils.toJson(objectMapper, body));
            }
            if (p.getId() == null) throw ErrorHelper.ex(ErrorCode.REMOTE_INVALID_RESPONSE, "userinfo_missing_id");
            return p;
        } catch (MiniAppException e) {
            throw e;
        } catch (Exception e) {
            throw ErrorHelper.ex(ErrorCode.REMOTE_INVALID_RESPONSE, "userinfo_parse_error", e);
        }
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
}
