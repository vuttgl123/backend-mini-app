package example.backend_mini_app.model.entity;

import example.backend_mini_app.model.enumeration.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Entity
@Schema(description = "Thông tin định danh người dùng từ nhà cung cấp bên ngoài (Zalo, Google, v.v.)")
@Table(name = "user_identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_provider_user", columnNames = {"provider", "provider_user_id"})
        },
        indexes = {
                @Index(name = "idx_ui_user", columnList = "user_id")
        })
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID định danh", example = "1")
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ui_user"))
    @Schema(description = "Người dùng liên kết với định danh này")
    private User user;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Schema(description = "Nhà cung cấp định danh", example = "ZALO")
    private Provider provider;

    @Setter
    @Column(name = "provider_user_id", nullable = false, length = 64)
    @Schema(description = "ID người dùng từ nhà cung cấp", example = "zalo_123456")
    private String providerUserId;

    @Setter
    @Column(name = "provider_union_id", length = 64)
    @Schema(description = "Union ID từ nhà cung cấp (nếu có)", example = "union_abc123")
    private String providerUnionId;

    @Setter
    @Column(name = "access_token", length = 512)
    @Schema(description = "Access token từ nhà cung cấp", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Setter
    @Column(name = "refresh_token", length = 512)
    @Schema(description = "Refresh token từ nhà cung cấp", example = "refresh_token_xyz")
    private String refreshToken;

    @Setter
    @Column(name = "token_scope", length = 255)
    @Schema(description = "Scope của token", example = "profile,email")
    private String tokenScope;

    @Setter
    @Column(name = "token_expires_at")
    @Schema(description = "Thời điểm token hết hạn", example = "2025-10-20T12:00:00Z")
    private Instant tokenExpiresAt;

    @Setter
    @Column(name = "raw_profile", columnDefinition = "jsonb")
    @Schema(description = "Thông tin profile gốc từ nhà cung cấp (dạng JSON)", example = "{\"name\":\"Nguyễn Văn A\"}")
    private String rawProfileJson;

    @Setter
    @Column(name = "first_linked_at", nullable = false)
    @Schema(description = "Thời điểm liên kết lần đầu", example = "2025-10-19T12:00:00Z")
    private Instant firstLinkedAt = Instant.now();

    @Setter
    @Column(name = "last_login_at")
    @Schema(description = "Thời điểm đăng nhập gần nhất", example = "2025-10-19T15:30:00Z")
    private Instant lastLoginAt;

    public UserIdentity() {}
}