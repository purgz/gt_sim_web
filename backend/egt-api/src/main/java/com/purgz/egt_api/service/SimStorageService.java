package com.purgz.egt_api.service;


// Responsible for checking the redis cache and object storage before running a new simulation

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.purgz.egt_api.model.Simulation;
import com.purgz.egt_api.model.User;
import com.purgz.egt_api.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class SimStorageService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MinioService minioService;
    private final SimulationRepository simulationRepository;
    private final ObjectMapper objectMapper;

    private static final Duration CACHE_TTL = Duration.ofHours(1);
    private static final String CACHE_PREFIX = "sim:";


    // Hash the method params to form a key for the redis store
    public String hashParams(Map<String, Object> params){

        try{
            Map<String, Object> sorted = new TreeMap<>(params);
            String json = objectMapper.writeValueAsString(sorted);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash){
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e){
            throw new RuntimeException("Failed to hash params");
        }
    }

    public Optional<Map<String, Object>> getFromCache(String hash) {
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_PREFIX + hash);
            if (cached == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(cached,
                    new TypeReference<>() {}));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Map<String, Object>> getFromStorage(String hash) {
        return simulationRepository.findByParamHash(hash).map(sim -> {
            try {
                String json = minioService.fetch(sim.getMinioKey());
                Map<String, Object> result = objectMapper.readValue(json,
                        new TypeReference<>() {});
                cacheResult(hash, result);
                return result;
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch simulation result", e);
            }
        });
    }

    public void store(String hash, Map<String, Object> params,
                      Map<String, Object> result, User createdBy) {
        try {
            String minioKey = "simulations/" + hash + ".json";
            String json = objectMapper.writeValueAsString(result);

            minioService.save(minioKey, json);
            cacheResult(hash, result);

            Simulation sim = new Simulation();
            sim.setParamHash(hash);
            sim.setMinioKey(minioKey);
            sim.setProcess((String) params.get("process"));
            sim.setPopSize((Integer) params.get("pop_size"));
            sim.setIterations((Integer) params.get("iterations"));
            sim.setSimulations((Integer) params.get("simulations"));
            sim.setW(((Number) params.get("w")).doubleValue());
            sim.setMatrix(objectMapper.writeValueAsString(params.get("matrix")));
            sim.setCreatedBy(createdBy);
            simulationRepository.save(sim);

        } catch (Exception e) {
            throw new RuntimeException("Failed to store simulation", e);
        }
    }

    private void cacheResult(String hash, Map<String, Object> result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(CACHE_PREFIX + hash, json, CACHE_TTL);
        } catch (Exception e) {
            // cache failure is non-fatal
        }
    }

}
