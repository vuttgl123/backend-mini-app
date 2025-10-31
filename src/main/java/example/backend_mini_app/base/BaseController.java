package example.backend_mini_app.base;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
public abstract class BaseController<T extends BaseEntity, RQ, RS> {


    protected final BaseService<T, RQ, RS> service;


    protected BaseController(BaseService<T, RQ, RS> service) {
        this.service = service;
    }


    @PostMapping
    public ApiResponse<RS> create(@RequestBody RQ request) {
        return ApiResponse.ok(service.create(request));
    }


    @GetMapping("/{id}")
    public ApiResponse<RS> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ApiResponse::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
    }


    @GetMapping
    public ApiResponse<PageResponse<RS>> getPage(@RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer size,
                                                 @RequestParam(required = false) String sortBy,
                                                 @RequestParam(required = false) String direction) {
        var pq = new PageQuery(page, size, sortBy,
                direction == null ? null : org.springframework.data.domain.Sort.Direction.fromOptionalString(direction).orElse(null));
        Page<RS> p = service.getPage(pq.toPageable());
        return ApiResponse.ok(PageResponse.fromPage(p));
    }


    @PutMapping("/{id}")
    public ApiResponse<RS> update(@PathVariable Long id, @RequestBody RQ request) {
        return ApiResponse.ok(service.update(id, request));
    }


    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ApiResponse.ok(null);
    }

    // ===================== HOW TO USE (example) =====================
// 1) Define your domain entity extending BaseEntity.
// package com.example.user;
// @Entity
// public class User extends BaseEntity { private String name; private String email; /* getters/setters */ }
//
// 2) Create a repository extending BaseRepository<User>.
// public interface UserRepository extends BaseRepository<User> {}
//
// 3) Define request/response DTOs.
// public record UserRequest(String name, String email) {}
// public record UserResponse(Long id, String name, String email) {}
//
// 4) Implement Mapper<UserRequest, User, UserResponse>.
// @Component
// public class UserMapper implements Mapper<UserRequest, User, UserResponse> {
// public User toEntity(UserRequest rq) { var u = new User(); u.setName(rq.name()); u.setEmail(rq.email()); return u; }
// public void updateEntity(User u, UserRequest rq) { u.setName(rq.name()); u.setEmail(rq.email()); }
// public UserResponse toResponse(User u) { return new UserResponse(u.getId(), u.getName(), u.getEmail()); }
// }
//
// 5) Create a service by extending BaseServiceImpl.
// @Service
// public class UserService extends BaseServiceImpl<User, UserRequest, UserResponse> {
// public UserService(UserRepository repo, UserMapper mapper) { super(repo, mapper); }
// }
//
// 6) Expose a controller by extending BaseController.
// @RequestMapping("/api/users")
// @RestController
// public class UserController extends BaseController<User, UserRequest, UserResponse> {
// public UserController(UserService service) { super(service); }
// }
}
