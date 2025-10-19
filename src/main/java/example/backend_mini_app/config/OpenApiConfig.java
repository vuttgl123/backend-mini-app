package example.backend_mini_app.config;

import example.backend_mini_app.marker.InternalApi;
import example.backend_mini_app.marker.PublicApi;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.filters.OpenApiMethodFilter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.function.Predicate;

@Configuration
@EnableConfigurationProperties(DocsProperties.class)
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MiniApp API")
                        .version("v1"));
    }

    @Bean
    List<GroupedOpenApi> groupedOpenApis(DocsProperties props) {
        List<GroupedOpenApi> result = new ArrayList<>();
        if (props.groups() == null) return result;

        for (DocsProperties.Group g : props.groups()) {
            if (!g.enabled()) continue;

            GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                    .group(g.name());

            if (g.includePaths() != null && !g.includePaths().isEmpty()) {
                builder.pathsToMatch(g.includePaths().toArray(String[]::new));
            }
            if (g.excludePaths() != null && !g.excludePaths().isEmpty()) {
                builder.pathsToExclude(g.excludePaths().toArray(String[]::new));
            }
            if (g.packages() != null && !g.packages().isEmpty()) {
                builder.packagesToScan(g.packages().toArray(String[]::new));
            }
            if ((g.packages() == null || g.packages().isEmpty())
                    && (g.includePaths() == null || g.includePaths().isEmpty())) {
                builder.pathsToMatch("/**");
            }

            OpenApiMethodFilter markerFilter = method -> {
                Predicate<Class<?>> hasPublic = cls ->
                        cls.isAnnotationPresent(PublicApi.class);
                Predicate<Class<?>> hasInternal = cls ->
                        cls.isAnnotationPresent(InternalApi.class);
                Class<?> controller = method.getDeclaringClass();

                return switch (g.marker()) {
                    case publicApi -> hasPublic.test(controller);
                    case internalApi -> hasInternal.test(controller);
                    case none -> true;
                };
            };
            builder.addOpenApiMethodFilter(markerFilter);

            builder.addOpenApiCustomizer(openApiCustomizerForGroup(g));

            result.add(builder.build());
        }
        return result;
    }

    private OpenApiCustomizer openApiCustomizerForGroup(DocsProperties.Group g) {
        return openApi -> {
            // Servers
            if (g.servers() != null && !g.servers().isEmpty()) {
                List<Server> servers = new ArrayList<>();
                for (String url : g.servers()) {
                    servers.add(new Server().url(url));
                }
                openApi.setServers(servers);
            }

            if (g.requireJwt()) {
                Components components = Optional.ofNullable(openApi.getComponents()).orElseGet(Components::new);
                components.addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization"));
                openApi.setComponents(components);

                SecurityRequirement requirement = new SecurityRequirement().addList(BEARER);
                List<SecurityRequirement> reqs = Optional.ofNullable(openApi.getSecurity()).orElseGet(ArrayList::new);
                reqs.add(requirement);
                openApi.setSecurity(reqs);
            }
        };
    }
}
