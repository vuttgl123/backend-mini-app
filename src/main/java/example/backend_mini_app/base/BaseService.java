package example.backend_mini_app.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BaseService<T extends BaseEntity, RQ, RS> {
    RS create(RQ request);
    Optional<RS> getById(Long id);
    Page<RS> getPage(Pageable pageable);
    RS update(Long id, RQ request);
    void softDelete(Long id);
}
