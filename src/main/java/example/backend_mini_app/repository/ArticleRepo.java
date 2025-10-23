package example.backend_mini_app.repository;

import example.backend_mini_app.model.entity.Article;
import example.backend_mini_app.model.enumeration.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleRepo extends JpaRepository<Article, Long> {
    Optional<Article> findBySlug(String slug);
    Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status);

    Page<Article> findByStatusAndTitleContainingIgnoreCaseOrStatusAndExcerptContainingIgnoreCase(
            ArticleStatus s1, String q1, ArticleStatus s2, String q2, Pageable pageable);
}
