package example.backend_mini_app.base;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageQuery(Integer page, Integer size, String sortBy, Sort.Direction direction) {
    public Pageable toPageable() {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size <= 0 || size > 200 ? 20 : size;
        String sb = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction dir = direction == null ? Sort.Direction.DESC : direction;
        return PageRequest.of(p, s, Sort.by(dir, sb));
    }
}