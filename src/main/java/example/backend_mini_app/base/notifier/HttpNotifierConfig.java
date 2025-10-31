package example.backend_mini_app.base.notifier;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(Notifier.class)
public class HttpNotifierConfig {
    @Bean
    public FilterRegistrationBean<HttpLoggingFilter> httpLoggingFilter(Notifier notifier, NotifierProperties props) {
        FilterRegistrationBean<HttpLoggingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new HttpLoggingFilter(notifier, props));
        bean.setOrder(Integer.MIN_VALUE + 10); // run early
        bean.addUrlPatterns("/*");
        return bean;
    }
}
