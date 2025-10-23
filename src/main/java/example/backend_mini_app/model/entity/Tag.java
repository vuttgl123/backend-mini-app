package example.backend_mini_app.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "tags",
        uniqueConstraints = @UniqueConstraint(name = "uk_tags_slug", columnNames = "slug")
)
@Getter
@Setter
@Schema(description = "Thẻ phân loại bài viết")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID tự tăng của thẻ", example = "1")
    private Long id;

    @Column(length = 100, nullable = false)
    @Schema(description = "Tên hiển thị của thẻ", example = "Thời sự")
    private String name;

    @Column(length = 120, nullable = false)
    @Schema(description = "Slug dùng cho URL", example = "thoi-su")
    private String slug;
}
