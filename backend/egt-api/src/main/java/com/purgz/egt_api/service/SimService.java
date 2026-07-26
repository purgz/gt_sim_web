package com.purgz.egt_api.service;


import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SimService {

    private final WebClient simServiceClient;

    public Map<String, Object> runSimulation(Map<String, Object> request){

        return simServiceClient.post()
                .uri("/sim/run")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofMinutes(10))
                .block();
    }

    public Map<String, Object> runReplicator(Map<String, Object> request) {
        return simServiceClient.post()
                .uri("/replicator/trajectory")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofMinutes(5))
                .block();
    }

    public Map<String, Object> runFokkerPlanck(Map<String, Object> request) {
        return simServiceClient.post()
                .uri("/replicator/fokker-planck")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofMinutes(5))
                .block();
    }

    public Map<String, Object> runDeltaHRange(Map<String, Object> request) {
        return simServiceClient.post()
                .uri("/analysis/delta-h-range")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofMinutes(15))
                .block();
    }

    public Map<String, Object> getCriticalNAnalytical(Map<String, Object> request) {
        return simServiceClient.post()
                .uri("/analysis/critical-n/analytical")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofMinutes(2))
                .block();
    }

    public Map<String, Object> getFixedPoint(Map<String, Object> request) {
        return simServiceClient.post()
                .uri("/analysis/fixed-point")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofMinutes(2))
                .block();
    }

    public boolean isSimServiceAvailable() {
        try {
            simServiceClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(3))
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
