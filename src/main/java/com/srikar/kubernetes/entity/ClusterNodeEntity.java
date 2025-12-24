package com.srikar.kubernetes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "cluster_nodes",
        schema = "iaas_kubernetes",
        indexes = {
                @Index(name = "ix_cluster_nodes_cluster_id", columnList = "cluster_id"),
                @Index(name = "ix_cluster_nodes_node_name", columnList = "node_name")
        }
)
public class ClusterNodeEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    // ✅ FK to clusters.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cluster_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cluster_nodes_cluster"))
    private ClusterEntity cluster;

    @Column(name = "node_name", nullable = false, length = 200)
    private String nodeName;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "roles", length = 500)
    private String roles;

    @Column(name = "kube_version", length = 50)
    private String kubeVersion;

    @Column(name = "container_runtime", length = 200)
    private String containerRuntime;

    @Column(name = "os_image", length = 300)
    private String osImage;

    @Column(name = "kernel_version", length = 100)
    private String kernelVersion;

    @Column(name = "is_control_plane")
    private Boolean isControlPlane;

    @Column(name = "is_vm")
    private Boolean isVm;

    @Column(name = "vm_name", length = 200)
    private String vmName;

    // ✅ IMPORTANT: map Postgres inet -> InetAddress
    @Column(name = "internal_ip", columnDefinition = "inet")
    private InetAddress internalIp;

    @Column(name = "external_ip", columnDefinition = "inet")
    private InetAddress externalIp;

    @Column(name = "observed_at")
    private Instant observedAt;
}
