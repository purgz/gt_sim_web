package com.purgz.egt_api.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.purgz.egt_api.model.Simulation;
import com.purgz.egt_api.model.User;
import com.purgz.egt_api.repository.SimulationRepository;
import com.purgz.egt_api.repository.UserRepository;
import com.purgz.egt_api.service.MinioService;
import com.purgz.egt_api.service.SimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sim")
@RequiredArgsConstructor
public class SimController {

    private final SimService simService;
    private final UserRepository userRepository;
    private final SimulationRepository simulationRepository;
    private final MinioService minioService;
    private final ObjectMapper objectMapper;


    // Get all the meta data for previously run results
    @GetMapping("/saved")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getSavedSimulations() {
        return ResponseEntity.ok(simulationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(sim -> Map.<String, Object>of(
                        "id", sim.getId(),
                        "process", sim.getProcess(),
                        "pop_size", sim.getPopSize(),
                        "w", sim.getW(),
                        "created_at", sim.getCreatedAt()
                ))
                .toList());
    }

    // Get a specific saved run from the object storage or cache
    @GetMapping("/saved/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getSavedSimulation(@PathVariable UUID id) {

        Simulation sim = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation not found"));
        String json = minioService.fetch(sim.getMinioKey());
        try {
            Map<String, Object> result = objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {});
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialise simulation result");
        }
    }



    @PostMapping("/run")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> runSimulation(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(simService.runSimulation(request, user));
    }

    @PostMapping("/replicator")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> runReplicator(
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(simService.runReplicator(request));
    }

    @PostMapping("/fokker-planck")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> runFokkerPlanck(
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(simService.runFokkerPlanck(request));
    }

    @PostMapping("/delta-h-range")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> deltaHRange(
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(simService.runDeltaHRange(request));
    }

    @PostMapping("/critical-n")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> criticalN(
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(simService.getCriticalNAnalytical(request));
    }

    @PostMapping("/fixed-point")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> fixedPoint(
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(simService.getFixedPoint(request));
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> simHealth() {
        boolean available = simService.isSimServiceAvailable();
        return ResponseEntity.ok(Map.of(
                "sim_service_available", available,
                "status", available ? "up" : "down"
        ));
    }
}
