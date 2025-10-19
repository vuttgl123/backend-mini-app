package example.backend_mini_app.repository;

import example.backend_mini_app.model.entity.OAuthState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthStateRepository extends JpaRepository<OAuthState, Long> {
    Optional<OAuthState> findByState(String state);
}
