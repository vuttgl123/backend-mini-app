package example.backend_mini_app.config;

import example.backend_mini_app.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final JwtAuthenticationFilter jwtFilter;

    private static final String[] DOCS = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/api-docs/**", "/docs/**"
    };
    private static final String[] STATIC_ASSETS = {
            "/", "/index.html", "/favicon.ico", "/assets/**", "/static/**"
    };


    private static final String[] HEALTH_INFO = { "/actuator/health", "/actuator/info" };
    private static final String[] ACTUATOR_ALL = { "/actuator/**" };

    private static final String[] API_AUTH = {
            "/api/auth/**",
            "/api/v1/auth/**"
    };
    private static final String[] API_PUBLIC = {
            "/api/public/**",
            "/api/v1/public/**"
    };

    private static final String[] API_WEBHOOKS = {
            "/zalo/webhooks/**",
            "/zalo/api/webhooks/**",
            "/legal/terms"
    };

    private static final String[] API_INTERNAL = {
            "/api/internal/**", "/api/v1/internal/**"
    };
    private static final String[] API_ADMIN = {
            "/api/admin/**", "/api/v1/admin/**"
    };
    private static final String[] API_PARTNER = {
            "/api/partner/**", "/api/v1/partner/**"
    };
    private static final String[] API_MOBILE = {
            "/api/mobile/**", "/api/v1/mobile/**"
    };
    private static final String[] API_WEATHER = {
            "/api/weather/**"
    };
    private static final String[] API_EVENT = {
            "/api/events/**"
    };

    private static final String[] API_GRAPHQL = { "/graphql", "/graphiql/**" };
    private static final String[] API_FILES   = { "/api/files/**", "/api/v1/files/**" };
    private static final String[] API_PAYMENTS= { "/api/payments/**", "/api/v1/payments/**" };
    private static final String[] API_REPORTS = { "/api/reports/**", "/api/v1/reports/**" };
    private static final String[] API_ANALYTICS = { "/api/analytics/**", "/api/v1/analytics/**" };
    private static final String[] API_NOTIFICATIONS = { "/api/notifications/**", "/api/v1/notifications/**" };
    private static final String[] API_INTEGRATIONS = { "/api/integrations/**", "/api/v1/integrations/**" };

    @Bean
    @Order(0)
    SecurityFilterChain zaloVerify(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/zalo_verifier*.html")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain docsAndStatic(HttpSecurity http) throws Exception {
        http
                .securityMatcher(DOCS)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain staticAssets(HttpSecurity http) throws Exception {
        http
                .securityMatcher(STATIC_ASSETS)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain actuator(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, HEALTH_INFO).permitAll()
                        .requestMatchers(ACTUATOR_ALL).hasRole("ADMIN")
                        .anyRequest().denyAll()
                );
        return http.build();
    }

    @Bean
    @Order(5)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**", "/webhooks/**", "/graphql", "/graphiql/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(API_AUTH).permitAll()
                        .requestMatchers(API_PUBLIC).permitAll()
                        .requestMatchers(API_WEBHOOKS).permitAll()
                        .requestMatchers(API_WEATHER).permitAll()
                        .requestMatchers(API_EVENT).permitAll()


                        .requestMatchers(API_GRAPHQL).authenticated()

                        .requestMatchers(API_FILES).authenticated()
                        .requestMatchers(API_PAYMENTS).hasAnyRole("ADMIN","FINANCE")
                        .requestMatchers(API_REPORTS).hasAnyRole("ADMIN","ANALYST")
                        .requestMatchers(API_ANALYTICS).hasAnyRole("ADMIN","ANALYST")
                        .requestMatchers(API_NOTIFICATIONS).hasAnyRole("ADMIN","OPERATOR")
                        .requestMatchers(API_INTEGRATIONS).hasAnyRole("ADMIN","INTEGRATION")

                        .requestMatchers(API_INTERNAL).hasAnyRole("INTERNAL","ADMIN")
                        .requestMatchers(API_PARTNER).hasAnyRole("PARTNER","ADMIN")
                        .requestMatchers(API_ADMIN).hasRole("ADMIN")
                        .requestMatchers(API_MOBILE).authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(99)
    SecurityFilterChain fallback(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().denyAll()
                );
        return http.build();
    }
}
