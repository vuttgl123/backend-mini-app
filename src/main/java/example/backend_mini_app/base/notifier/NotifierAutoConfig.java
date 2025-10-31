package example.backend_mini_app.base.notifier;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NotifierProperties.class)
public class NotifierAutoConfig {
    @Bean
    @ConditionalOnMissingBean(Sink.class)
    public Sink logSink() { return new LogSink(); }


    @Bean @ConditionalOnMissingBean(Notifier.class)
    public Notifier notifier(Sink sink, NotifierProperties props) { return new DefaultNotifier(sink, props); }
}
