package example.backend_mini_app;

import example.backend_mini_app.config.ZaloOAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ZaloOAuthProperties.class})
public class BackendMiniAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendMiniAppApplication.class, args);
	}

}