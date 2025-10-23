package example.backend_mini_app.service.impl;

import example.backend_mini_app.service.ContentRenderService;
import org.springframework.stereotype.Service;

@Service
public class ContentRenderServiceImpl implements ContentRenderService {

    @Override
    public String renderAndSanitize(String markdown) {
        return markdown
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;");
    }
}
