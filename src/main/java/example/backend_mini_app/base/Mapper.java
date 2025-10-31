package example.backend_mini_app.base;

public interface Mapper<RQ, T extends BaseEntity, RS> {
    T toEntity(RQ request);
    void updateEntity(T entity, RQ request);
    RS toResponse(T entity);
}
