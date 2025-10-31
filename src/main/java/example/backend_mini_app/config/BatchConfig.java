package example.backend_mini_app.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfig {


    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository, TaskExecutor taskExecutor) throws Exception {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        // dùng async executor nếu muốn chạy job bất đồng bộ
        launcher.setTaskExecutor(taskExecutor);
        launcher.afterPropertiesSet();
        return launcher;
    }


    // Gợi ý: TransactionManager nên là JpaTransactionManager cấu hình ở layer persistence của bạn
    @Bean
    public BatchUtils batchUtils(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new BatchUtils(jobRepository, tx);
    }
}
