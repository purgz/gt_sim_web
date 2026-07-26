package com.purgz.egt_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SimServiceConfig {

    @Value("${sim.service.url}")
    private String simServiceUrl;

    @Value("${sim.service.token}")
    private String simServiceToken;

    @Bean
    public WebClient simServiceClient(){
        return WebClient.builder()
                .baseUrl(simServiceUrl)
                .defaultHeader("x-internal-token", simServiceToken)
                .build();
    }
}
