package example.backend_mini_app.model.entity;

import example.backend_mini_app.model.enumeration.ArticleStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "articles",
        uniqueConstraints = @UniqueConstraint(name = "uk_articles_slug", columnNames = "slug"),
        indexes = {
                @Index(name = "ix_articles_status_published_at", columnList = "status, published_at DESC")
        }
)
@Getter
@Setter
@Schema(description = "Bài viết tin tức")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID tự tăng của bài viết", example = "1")
    private Long id;

    @Column(length = 200, nullable = false)
    @Schema(description = "Slug dùng cho URL", example = "cap-nhat-quy-dinh-gplx-2025")
    private String slug;

    @Column(length = 300, nullable = false)
    @Schema(description = "Tiêu đề bài viết", example = "Cập nhật quy định GPLX 2025")
    private String title;

    @Column(length = 500)
    @Schema(description = "Tóm tắt nội dung bài viết", example = "Một số điểm thay đổi về sát hạch và thời hạn bằng lái.")
    private String excerpt;

    @Lob
    @Column(name="body_md", nullable = false, columnDefinition = "MEDIUMTEXT")
    @Schema(description = "Nội dung bài viết ở định dạng Markdown", example = "# Quy định mới\n\n- Điểm A\n- Điểm B")
    private String bodyMd;

    @Lob
    @Column(name="body_html", columnDefinition = "MEDIUMTEXT")
    @Schema(description = "Nội dung bài viết ở định dạng HTML đã được sanitize", example = "<h1>Quy định mới</h1><ul><li>Điểm A</li><li>Điểm B</li></ul>")
    private String bodyHtml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @ColumnDefault("'DRAFT'")
    @Schema(description = "Trạng thái bài viết", example = "PUBLISHED")
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(length = 100, nullable = false)
    @Schema(description = "Tác giả bài viết", example = "admin")
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_media_id",
            foreignKey = @ForeignKey(name = "fk_articles_cover"))
    @Schema(description = "Ảnh đại diện của bài viết")
    private Media coverMedia;

    @Column(name="source_name", length = 200)
    @Schema(description = "Tên nguồn tham khảo", example = "Bộ GTVT")
    private String sourceName;

    @Column(name="source_url", length = 500)
    @Schema(description = "URL nguồn tham khảo", example = "https://www.example.gov.vn/van-ban/abc")
    private String sourceUrl;

    @Column(name="published_at")
    @Schema(description = "Thời điểm bài viết được publish", example = "2025-10-23T10:15:45")
    private LocalDateTime publishedAt;

    @Column(name="created_at", nullable = false, updatable = false)
    @Schema(description = "Thời điểm tạo bài viết", example = "2025-10-23T10:15:45")
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    @Schema(description = "Thời điểm cập nhật bài viết", example = "2025-10-23T10:15:45")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "article_tag",
            joinColumns = @JoinColumn(name = "article_id",
                    foreignKey = @ForeignKey(name = "fk_at_article")),
            inverseJoinColumns = @JoinColumn(name = "tag_id",
                    foreignKey = @ForeignKey(name = "fk_at_tag"))
    )
    @Schema(description = "Danh sách các thẻ gắn với bài viết")
    private Set<Tag> tags = new LinkedHashSet<>();

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == ArticleStatus.PUBLISHED && publishedAt == null) {
            publishedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == ArticleStatus.PUBLISHED && publishedAt == null) {
            publishedAt = updatedAt;
        }
    }
}