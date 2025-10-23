package example.backend_mini_app.controller;

import example.backend_mini_app.model.request.ArticleRequest;
import example.backend_mini_app.model.response.ArticleResponse;
import example.backend_mini_app.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class ArticleAdminController {
    private final ArticleService articleService;

    @PostMapping
    public ArticleResponse create(@RequestBody ArticleRequest req, Principal p) {
        return articleService.create(req, p != null ? p.getName() : "admin", true);
    }

    @PatchMapping("/{id}")
    public ArticleResponse patch(@PathVariable Long id, @RequestBody ArticleRequest req) {
        return articleService.patch(id, req, true);
    }

    @PostMapping("/{id}/publish")
    public ArticleResponse publish(@PathVariable Long id) {
        return articleService.publish(id);
    }

    @GetMapping("/{id}")
    public ArticleResponse get(@PathVariable Long id) {
        return articleService.getAdminById(id);
    }
}

