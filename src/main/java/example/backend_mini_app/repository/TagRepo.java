package example.backend_mini_app.repository;

import example.backend_mini_app.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepo extends JpaRepository<Tag, Long> {
    Optional<Tag> findBySlug(String slug);
}
