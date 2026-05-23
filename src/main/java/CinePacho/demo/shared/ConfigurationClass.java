package CinePacho.demo.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ConfigurationClass {

    @Bean
    WebClient webClient() {
        return WebClient.builder().
                baseUrl("https://api.themoviedb.org/3")
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(1024*1024))
                .build();

    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);  // 5 timers corriendo en paralelo
        scheduler.setThreadNamePrefix("seat-unblock-");
        scheduler.initialize();
        return scheduler;
    }

}
