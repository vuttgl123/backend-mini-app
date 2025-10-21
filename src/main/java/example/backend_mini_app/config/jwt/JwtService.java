package example.backend_mini_app.config.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JwtService {

    String generateAccessToken(String username, List<String> roles);

    String generateRefreshToken(String username);

    boolean isExpired(String token);

    String getUsername(String token) throws JwtException;

    Collection<? extends GrantedAuthority> getAuthorities(String token) throws JwtException;

    void validate(String token) throws JwtException;

    Map<String, Object> getClaims(String token) throws JwtException;
}