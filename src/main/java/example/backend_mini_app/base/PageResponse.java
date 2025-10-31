package example.backend_mini_app.base;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
    public static <T> PageResponse<T> fromPage(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}