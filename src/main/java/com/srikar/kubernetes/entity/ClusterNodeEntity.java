package com.srikar.kubernetes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cluster_nodes", indexes = {
        @Index(name = "idx_cluster_nodes_cluster_id", columnList = "cluster_id"),
        @Index(name = "idx_cluster_nodes_name", columnList = "name")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cluster_nodes_cluster_name", columnNames = {"cluster_id", "name"})
})
public class ClusterNodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many nodes belong to one cluster
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private ClusterEntity cluster;

    @Column(nullable = false, length = 128)
    private String name;      // host / oneinfra-node1 / oneinfra-node2

    @Column(nullable = false, length = 32)
    private String status;    // Ready / NotReady etc.

    @Column(nullable = false, length = 64)
    private String roles;     // "control-plane,master" or "worker"

    @Column(nullable = false, length = 32)
    private String version;   // v1.30.14

    @Column(nullable = false)
    private Instant observedAt;  // when this row was last refreshed
}
