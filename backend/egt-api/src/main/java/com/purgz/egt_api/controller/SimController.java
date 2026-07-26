package com.purgz.egt_api.controller;

import com.purgz.egt_api.service.SimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sim")
@RequiredArgsConstructor
public class SimController {

    private final SimService simService;

    @PostMapping("/run")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> runSimulation(
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(simService.runSimulation(request));
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
