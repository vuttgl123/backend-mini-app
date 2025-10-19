package example.backend_mini_app.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class JwtService {

    private final SecretKey key;
    private final JwtParser parser;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.parser = Jwts.parserBuilder().setSigningKey(this.key).build();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return parser.parseClaimsJws(token);
    }

    public String getUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        var claims = parse(token).getBody();
        var roles = (List<String>) claims.getOrDefault("roles", List.of());
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    public boolean isExpired(String token) {
        Date exp = parse(token).getBody().getExpiration();
        return exp != null && exp.before(new Date());
    }
}
