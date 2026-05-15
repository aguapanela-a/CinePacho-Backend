package CinePacho.demo.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
}
