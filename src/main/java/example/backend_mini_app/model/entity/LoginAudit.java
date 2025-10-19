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
@Schema(description = "Lịch sử đăng nhập của người dùng")
@Table(name = "login_audit",
        indexes = {
                @Index(name = "idx_la_user", columnList = "user_id, created_at")
        })
public class LoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID bản ghi đăng nhập", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_la_user"))
    @Schema(description = "Người dùng thực hiện đăng nhập")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Schema(description = "Nhà cung cấp xác thực", example = "ZALO")
    private Provider provider;

    @Column(nullable = false)
    @Schema(description = "Kết quả đăng nhập", example = "true")
    private boolean success;

    @Column(length = 255)
    @Schema(description = "Lý do thất bại (nếu có)", example = "Sai mã xác thực")
    private String reason;

    @Column(length = 45)
    @Schema(description = "Địa chỉ IP của người dùng", example = "192.168.1.100")
    private String ip;

    @Column(name = "user_agent", length = 255)
    @Schema(description = "Thông tin User-Agent của trình duyệt", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Thời điểm đăng nhập", example = "2025-10-19T14:30:00Z")
    private Instant createdAt = Instant.now();

    public LoginAudit() {}
}