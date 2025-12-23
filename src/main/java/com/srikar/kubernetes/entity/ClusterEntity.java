package com.srikar.kubernetes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clusters", uniqueConstraints = {
        @UniqueConstraint(name = "uk_clusters_name", columnNames = {"name"})
})
public class ClusterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;          // e.g., "oneinfra"

    @Column(length = 256)
    private String apiServer;     // optional, if you want to store API endpoint later

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClusterNodeEntity> nodes = new ArrayList<>();
}
