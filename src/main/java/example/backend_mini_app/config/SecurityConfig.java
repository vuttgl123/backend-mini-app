//package example.backend_mini_app.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//    private static final String[] SWAGGER_WHITELIST = {
//            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/docs", "/api-docs/**"
//    };
//
//    @Bean
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .cors(Customizer.withDefaults())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
//                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(Customizer.withDefaults()); // chỉ để test nhanh
//        return http.build();
//    }
//}
