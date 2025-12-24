package com.srikar.kubernetes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "clusters",
        schema = "iaas_kubernetes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cluster_name", columnNames = {"cluster_name"})
        }
)
public class ClusterEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "cluster_name", nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClusterNodeEntity> nodes = new ArrayList<>();
}
