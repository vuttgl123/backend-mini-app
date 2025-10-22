package example.backend_mini_app.config.jwt;

import example.backend_mini_app.config.property.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final JwtProperties props;

    @Override
    public String generateAccessToken(String username, List<String> roles) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(props.accessTokenTtlMinutes(), ChronoUnit.MINUTES))
                .subject(username)
                .claim("roles", roles != null ? roles : List.of())
                .claim("type", "access")
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public String generateRefreshToken(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(props.refreshTokenTtlDays(), ChronoUnit.DAYS))
                .subject(username)
                .claim("type", "refresh")
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public boolean isExpired(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            Instant exp = jwt.getExpiresAt();
            return exp != null && exp.isBefore(Instant.now());
        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return true; // Coi như expired nếu invalid
        }
    }

    @Override
    public String getUsername(String token) throws JwtException {
        return decoder.decode(token).getSubject();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(String token) throws JwtException {
        Jwt jwt = decoder.decode(token);
        Object raw = jwt.getClaims().get("roles");

        List<String> roles = extractRoles(raw);

        return roles.stream()
                .map(this::normalizeRole)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public void validate(String token) throws JwtException {
        decoder.decode(token);
    }

    @Override
    public Map<String, Object> getClaims(String token) throws JwtException {
        return decoder.decode(token).getClaims();
    }

    private List<String> extractRoles(Object raw) {
        if (raw instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .toList();
        }

        if (raw instanceof String str && StringUtils.hasText(str)) {
            return Arrays.stream(str.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();
        }

        return List.of();
    }

    private String normalizeRole(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}