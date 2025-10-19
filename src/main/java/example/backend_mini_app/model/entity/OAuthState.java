package example.backend_mini_app.model.entity;

import example.backend_mini_app.model.enumeration.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Schema(description = "Thông tin trạng thái OAuth dùng để xác thực qua nhà cung cấp bên ngoài")
@Table(name = "oauth_state",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_state", columnNames = "state")
        })
public class OAuthState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID trạng thái OAuth", example = "1")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Schema(description = "Nhà cung cấp OAuth", example = "ZALO")
    private Provider provider = Provider.ZALO;

    @Column(nullable = false, length = 36)
    @Schema(description = "Mã trạng thái OAuth (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String state;

    @Column(name = "code_verifier", length = 128)
    @Schema(description = "Mã xác minh dùng trong PKCE", example = "s3cr3tC0d3V3r1f13r")
    private String codeVerifier;

    @Column(name = "redirect_uri", nullable = false, length = 255)
    @Schema(description = "Đường dẫn redirect sau khi xác thực", example = "https://example.com/oauth/callback")
    private String redirectUri;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Thời điểm tạo trạng thái", example = "2025-10-19T12:00:00Z")
    private Instant createdAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    @Schema(description = "Thời điểm trạng thái hết hạn", example = "2025-10-19T12:10:00Z")
    private Instant expiresAt;

    @Column(name = "consumed_at")
    @Schema(description = "Thời điểm trạng thái đã được sử dụng", example = "2025-10-19T12:05:00Z")
    private Instant consumedAt;

    public OAuthState() {}
}