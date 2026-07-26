package com.purgz.egt_api.repository;

import com.purgz.egt_api.model.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, UUID> {
    Optional<Simulation> findByParamHash(String paramHash);
    List<Simulation> findAllByOrderByCreatedAtDesc();
}