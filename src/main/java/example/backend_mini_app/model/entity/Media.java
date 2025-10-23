package example.backend_mini_app.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "media",
        uniqueConstraints = @UniqueConstraint(name = "uk_media_object_key", columnNames = "object_key"),
        indexes = {
                @Index(name = "ix_media_mime_type", columnList = "mime_type")
        }
)
@Getter
@Setter
@Schema(description = "Thông tin file media đã upload")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID tự tăng", example = "1")
    private Long id;

    @Column(name="object_key", length = 512, nullable = false)
    @Schema(description = "Đường dẫn trong bucket", example = "news/2025/10/GPLX.jpg")
    private String objectKey;

    @Column(name="mime_type", length = 100, nullable = false)
    @Schema(description = "Loại MIME của file", example = "image/jpeg")
    private String mimeType;

    @Column(name="size_bytes", nullable = false)
    @Schema(description = "Kích thước file tính bằng byte", example = "55296")
    private long sizeBytes;

    @Column
    @Schema(description = "Chiều rộng (nếu là ảnh)", example = "1024")
    private Integer width;

    @Column
    @Schema(description = "Chiều cao (nếu là ảnh)", example = "768")
    private Integer height;

    @Column(name="checksum_sha256", length = 64)
    @Schema(description = "Checksum SHA-256 của file", example = "abc123...")
    private String checksumSha256;

    @Lob
    @Column(name="variants_json")
    @Schema(description = "Thông tin các biến thể của media (nếu có)", example = "{\"thumbnail\":\"...\"}")
    private String variantsJson;

    @Column(name="uploaded_by", length = 100, nullable = false)
    @Schema(description = "Người upload", example = "admin")
    private String uploadedBy;

    @Column(name="created_at", nullable = false, updatable = false)
    @Schema(description = "Thời điểm tạo", example = "2025-10-23T10:15:45")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
