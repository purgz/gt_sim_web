package com.purgz.egt_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SimServiceConfig {

    @Value("${sim.service.url}")
    private String simServiceUrl;

    @Value("${sim.service.token}")
    private String simServiceToken;

    @Value("${sim.service.max-buffer-bytes:33554432}")
    private int maxBufferBytes;

    @Bean
    public WebClient simServiceClient() {
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxBufferBytes))
                .build();

        return WebClient.builder()
                .baseUrl(simServiceUrl)
                .defaultHeader("x-internal-token", simServiceToken)
                .exchangeStrategies(strategies)
                .build();
    }
}
