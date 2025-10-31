package example.backend_mini_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "ioExecutor")
    public Executor ioExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(8);
        ex.setMaxPoolSize(32);
        ex.setQueueCapacity(1000);
        ex.setThreadNamePrefix("io-");
        ex.initialize();
        return ex;
    }


    @Bean(name = "cpuExecutor")
    public Executor cpuExecutor() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(cores);
        ex.setMaxPoolSize(cores * 2);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("cpu-");
        ex.initialize();
        return ex;
    }


    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("sched-");
        scheduler.initialize();
        return scheduler;
    }
}
