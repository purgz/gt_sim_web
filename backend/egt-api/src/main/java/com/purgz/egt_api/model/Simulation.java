package com.purgz.egt_api.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

// WE store the simulation meta data so users can retrieve the list of previously run simulations and select them
// It is not necessary in the current setup.

@Entity
@Table(name = "simulations")
@Getter
@Setter
@NoArgsConstructor
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "param_hash", unique = true, nullable = false, length = 64)
    private String paramHash;

    @Column(nullable = false)
    private String process;

    @Column(name = "pop_size")
    private Integer popSize;

    private Integer iterations;

    private Integer simulations;

    private Double w;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String matrix;

    @Column(name = "minio_key", nullable = false)
    private String minioKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
