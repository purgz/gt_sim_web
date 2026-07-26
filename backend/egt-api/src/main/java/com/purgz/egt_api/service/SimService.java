package com.purgz.egt_api.service;


import com.purgz.egt_api.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SimService {

    private final WebClient simServiceClient;
    private final SimStorageService simStorageService;

    public Map<String, Object> runSimulation(Map<String, Object> request, User user){

        String hash = simStorageService.hashParams(request);


        // First check the cache
        Optional<Map<String, Object>> cached = simStorageService.getFromCache(hash);
        if (cached.isPresent()){
            return withSource(cached.get(), "cache");
        }

        // Check object store
        // Whenever we get from storage the value is cached
        Optional<Map<String, Object>> stored = simStorageService.getFromStorage(hash);
        if (stored.isPresent()){
            return withSource(stored.get(), "storage");
        }

        // Run fully
        Map<String, Object> result = simServiceClient.post()
                                        .uri("/sim/run")
                                        .bodyValue(request)
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                        .timeout(Duration.ofMinutes(10))
                                        .block();
        simStorageService.store(hash, request, result, user);

        return withSource(result, "fresh");
    }

    // Just adding the source of the result to the object so we can see if its from cache, object db or freshly run. nice for testing
    public Map<String, Object> withSource(Map<String, Object> result, String source){
        Map<String, Object> wrapped = new HashMap<>(result);
        wrapped.put("source", source);
        return wrapped;
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
