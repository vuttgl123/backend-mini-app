package example.backend_mini_app.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public abstract class BaseServiceImpl<T extends BaseEntity, RQ, RS>
        implements BaseService<T, RQ, RS> {


    protected final BaseRepository<T> repository;
    protected final Mapper<RQ, T, RS> mapper;


    protected BaseServiceImpl(BaseRepository<T> repository, Mapper<RQ, T, RS> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public RS create(RQ request) {
        T entity = mapper.toEntity(request);
        T saved = repository.save(entity);
        return mapper.toResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<RS> getById(Long id) {
        return repository.findById(id)
                .filter(e -> !e.isDeleted())
                .map(mapper::toResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<RS> getPage(Pageable pageable) {
        return repository.findAllByDeletedFalse(pageable).map(mapper::toResponse);
    }


    @Override
    public RS update(Long id, RQ request) {
        T entity = repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
        mapper.updateEntity(entity, request);
        T saved = repository.save(entity);
        return mapper.toResponse(saved);
    }


    @Override
    public void softDelete(Long id) {
        T entity = repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
        entity.setDeleted(true);
        repository.save(entity);
    }
}
