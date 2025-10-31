package example.backend_mini_app.model.entity;

import example.backend_mini_app.model.enumeration.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Entity
@Schema(description = "Thông tin người dùng trong hệ thống")
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_phone", columnList = "phone")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_phone", columnNames = "phone")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID người dùng", example = "1")
    private Long id;

    @Setter
    @Column(length = 255, unique = true)
    @Schema(description = "Email người dùng", example = "user@example.com")
    private String email;

    @Setter
    @Column(length = 20, unique = true)
    @Schema(description = "Số điện thoại người dùng", example = "0987654321")
    private String phone;

    @Setter
    @Column(name = "display_name", length = 100)
    @Schema(description = "Tên hiển thị của người dùng", example = "Nguyễn Văn A")
    private String displayName;

    @Setter
    @Column(name = "avatar_url", columnDefinition = "text")
    @Schema(description = "URL ảnh đại diện", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Schema(description = "Trạng thái người dùng", example = "ACTIVE")
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Thời điểm tạo tài khoản", example = "2025-10-19T12:00:00Z")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Thời điểm cập nhật cuối cùng", example = "2025-10-19T12:00:00Z")
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Danh sách định danh liên kết với người dùng")
    private Set<UserIdentity> identities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Danh sách refresh token của người dùng")
    private Set<AuthRefreshToken> refreshTokens = new LinkedHashSet<>();

    public User() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addIdentity(UserIdentity identity) {
        identities.add(identity);
        identity.setUser(this);
    }

    public void removeIdentity(UserIdentity identity) {
        identities.remove(identity);
        identity.setUser(null);
    }

    public void addRefreshToken(AuthRefreshToken t) {
        refreshTokens.add(t);
        t.setUser(this);
    }

    public void removeRefreshToken(AuthRefreshToken t) {
        refreshTokens.remove(t);
        t.setUser(null);
    }
}
