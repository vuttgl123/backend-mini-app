package example.backend_mini_app.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Entity
@Schema(description = "Thông tin refresh token dùng để xác thực người dùng")
@Table(name = "auth_refresh_token",
        indexes = {
                @Index(name = "idx_art_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_token_hash", columnNames = "token_hash")
        })
public class AuthRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID của refresh token", example = "1")
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_art_user"))
    @Schema(description = "Người dùng sở hữu refresh token này")
    private User user;

    @Setter
    @Column(name = "token_hash", nullable = false, length = 64)
    @Schema(description = "Hash của refresh token", example = "a1b2c3d4e5f6...")
    private String tokenHash;

    @Setter
    @Column(name = "issued_at", nullable = false)
    @Schema(description = "Thời điểm token được cấp", example = "2025-10-19T12:00:00Z")
    private Instant issuedAt;

    @Setter
    @Column(name = "expires_at", nullable = false)
    @Schema(description = "Thời điểm token hết hạn", example = "2025-11-19T12:00:00Z")
    private Instant expiresAt;

    @Setter
    @Column(name = "revoked_at")
    @Schema(description = "Thời điểm token bị thu hồi (nếu có)", example = "2025-10-20T08:00:00Z")
    private Instant revokedAt;

    @Setter
    @Column(length = 45)
    @Schema(description = "Địa chỉ IP khi cấp token", example = "192.168.1.1")
    private String ip;

    @Setter
    @Column(name = "user_agent", length = 255)
    @Schema(description = "Thông tin User-Agent của trình duyệt", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

    @Setter
    @Column(name = "replaced_by")
    @Schema(description = "ID của token mới thay thế token này (nếu có)", example = "2")
    private Long replacedBy;

    public AuthRefreshToken() {}
}