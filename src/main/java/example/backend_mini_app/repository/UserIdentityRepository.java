package example.backend_mini_app.repository;

import example.backend_mini_app.model.entity.UserIdentity;
import example.backend_mini_app.model.enumeration.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
    Optional<UserIdentity> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}
