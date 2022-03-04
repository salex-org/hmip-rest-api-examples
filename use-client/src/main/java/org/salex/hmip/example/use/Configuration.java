package org.salex.hmip.example.use;

import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPConfiguration;
import org.salex.hmip.client.HmIPProperties;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Bean
    HmIPClient createHomematicClient(HmIPProperties properties) {
        return HmIPConfiguration.builder()
                .properties(properties)
                .build()
                .map(HmIPClient::new)
                .block();
    }
}
