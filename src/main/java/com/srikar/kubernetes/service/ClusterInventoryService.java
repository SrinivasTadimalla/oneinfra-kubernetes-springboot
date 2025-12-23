package com.srikar.kubernetes.service;

import com.srikar.kubernetes.entity.ClusterEntity;
import com.srikar.kubernetes.entity.ClusterNodeEntity;
import com.srikar.kubernetes.db.ClusterNodeRepository;
import com.srikar.kubernetes.db.ClusterRepository;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClusterInventoryService {

    private final KubernetesClient client;
    private final ClusterRepository clusterRepo;
    private final ClusterNodeRepository nodeRepo;

    @Transactional
    public ClusterEntity refreshCluster(String clusterName) {

        Instant now = Instant.now();

        ClusterEntity cluster = clusterRepo.findByName(clusterName)
                .orElseGet(() -> {
                    ClusterEntity c = ClusterEntity.builder()
                            .name(clusterName)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return clusterRepo.save(c);
                });

        cluster.setUpdatedAt(now);

        // wipe old nodes for this cluster
        nodeRepo.deleteByClusterId(cluster.getId());

        // re-insert snapshot
        var nodes = client.nodes().list().getItems().stream()
                .map(n -> {
                    String name = n.getMetadata() != null ? n.getMetadata().getName() : null;

                    // roles typically come from label: node-role.kubernetes.io/<role>
                    String roles = (n.getMetadata() != null && n.getMetadata().getLabels() != null)
                            ? n.getMetadata().getLabels().keySet().stream()
                            .filter(k -> k.startsWith("node-role.kubernetes.io/"))
                            .map(k -> k.replace("node-role.kubernetes.io/", ""))
                            .collect(Collectors.joining(","))
                            : "";

                    if (roles.isBlank()) roles = "worker"; // fallback if no role labels

                    String version = (n.getStatus() != null && n.getStatus().getNodeInfo() != null)
                            ? n.getStatus().getNodeInfo().getKubeletVersion()
                            : "—";

                    String status = (n.getStatus() != null && n.getStatus().getConditions() != null)
                            ? n.getStatus().getConditions().stream()
                            .filter(c -> "Ready".equalsIgnoreCase(c.getType()))
                            .findFirst()
                            .map(c -> "True".equalsIgnoreCase(c.getStatus()) ? "Ready" : "NotReady")
                            .orElse("Unknown")
                            : "Unknown";

                    return ClusterNodeEntity.builder()
                            .cluster(cluster)
                            .name(name)
                            .roles(roles)
                            .version(version)
                            .status(status)
                            .observedAt(now)
                            .build();
                })
                .toList();

        nodeRepo.saveAll(nodes);
        return clusterRepo.save(cluster);
    }
}
