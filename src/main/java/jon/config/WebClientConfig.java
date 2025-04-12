package jon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // Laver et WebClient-objekt, som bruges hver gang der skal bruges en WebClient.”
    // Den injiceres så i vores service-klasse
    // Sådanne objekter annoteres med @Bean og skal stå i en @Configuration-klasse
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}