package example.backend_mini_app.repository;

import example.backend_mini_app.model.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaRepo extends JpaRepository<Media, Long> {
    Optional<Media> findByObjectKey(String objectKey);
}
