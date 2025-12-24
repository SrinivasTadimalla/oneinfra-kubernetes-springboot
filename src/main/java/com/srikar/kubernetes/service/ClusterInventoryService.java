package com.srikar.kubernetes.service;

import com.srikar.kubernetes.db.ClusterNodeRepository;
import com.srikar.kubernetes.db.ClusterRepository;
import com.srikar.kubernetes.entity.ClusterEntity;
import com.srikar.kubernetes.entity.ClusterNodeEntity;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClusterInventoryService {

    private final KubernetesClient client;
    private final ClusterRepository clusterRepo;
    private final ClusterNodeRepository nodeRepo;

    @Transactional(readOnly = true)
    public List<ClusterEntity> getClusters() {
        return clusterRepo.findAll();
    }

    /**
     * ✅ Main entrypoint:
     * Controller triggers this to upsert the cluster + snapshot nodes.
     *
     * This is the equivalent of running:
     *   kubectl get nodes -o wide
     * and persisting the result.
     */
    @Transactional
    public ClusterEntity upsertClusterFromK8s(String clusterName) {
        if (clusterName == null || clusterName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cluster name is required");
        }

        Instant now = Instant.now();

        // ✅ Upsert cluster record
        ClusterEntity cluster = clusterRepo.findByName(clusterName)
                .orElseGet(() -> clusterRepo.save(
                        ClusterEntity.builder()
                                .id(UUID.randomUUID())
                                .name(clusterName)
                                .createdAt(now)
                                .updatedAt(now)
                                .build()
                ));

        cluster.setUpdatedAt(now);

        // ✅ Replace node snapshot
        nodeRepo.deleteByClusterId(cluster.getId());

        List<ClusterNodeEntity> nodes = client.nodes().list().getItems().stream()
                .map(n -> {

                    var meta = n.getMetadata();
                    var st = n.getStatus();

                    String nodeName = (meta != null) ? meta.getName() : null;
                    if (nodeName == null || nodeName.isBlank()) return null;

                    // roles from labels: node-role.kubernetes.io/<role>
                    String roles = (meta != null && meta.getLabels() != null)
                            ? meta.getLabels().keySet().stream()
                            .filter(k -> k.startsWith("node-role.kubernetes.io/"))
                            .map(k -> k.replace("node-role.kubernetes.io/", ""))
                            .filter(r -> !r.isBlank())
                            .collect(Collectors.joining(","))
                            : "";

                    if (roles.isBlank()) roles = "worker";

                    boolean isControlPlane = roles.contains("control-plane") || roles.contains("master");

                    String kubeVersion = (st != null && st.getNodeInfo() != null)
                            ? nullSafe(st.getNodeInfo().getKubeletVersion(), "—")
                            : "—";

                    String nodeStatus = (st != null && st.getConditions() != null)
                            ? st.getConditions().stream()
                            .filter(c -> "Ready".equalsIgnoreCase(c.getType()))
                            .findFirst()
                            .map(c -> "True".equalsIgnoreCase(c.getStatus()) ? "Ready" : "NotReady")
                            .orElse("Unknown")
                            : "Unknown";

                    Map<String, String> ips = extractNodeIps(st);
                    String internalIp = ips.get("InternalIP");
                    String externalIp = ips.get("ExternalIP");

                    // DB internal_ip is NOT NULL, so skip if missing
                    if (internalIp == null || internalIp.isBlank()) return null;

                    String osImage = (st != null && st.getNodeInfo() != null)
                            ? nullSafe(st.getNodeInfo().getOsImage(), "—")
                            : "—";

                    String kernelVersion = (st != null && st.getNodeInfo() != null)
                            ? nullSafe(st.getNodeInfo().getKernelVersion(), "—")
                            : "—";

                    String containerRuntime = (st != null && st.getNodeInfo() != null)
                            ? nullSafe(st.getNodeInfo().getContainerRuntimeVersion(), "—")
                            : "—";

                    // For now, infer VM based on your lab (workers are VMs; control-plane is host)
                    boolean isVm = !isControlPlane;
                    String vmName = isVm ? nodeName : null;

                    return ClusterNodeEntity.builder()
                            .id(UUID.randomUUID())
                            .cluster(cluster)
                            .name(nodeName)
                            .status(nodeStatus)
                            .roles(roles)
                            .kubeVersion(kubeVersion)
                            .internalIp(internalIp)
                            .externalIp(externalIp)
                            .osImage(osImage)
                            .kernelVersion(kernelVersion)
                            .containerRuntime(containerRuntime)
                            .controlPlane(isControlPlane)
                            .vm(isVm)
                            .vmName(vmName)
                            .observedAt(now)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        nodeRepo.saveAll(nodes);

        return clusterRepo.save(cluster);
    }

    // Keep old method name if your controller still calls refreshCluster(...)
    @Transactional
    public ClusterEntity refreshCluster(String clusterName) {
        return upsertClusterFromK8s(clusterName);
    }

    private static Map<String, String> extractNodeIps(io.fabric8.kubernetes.api.model.NodeStatus status) {
        Map<String, String> m = new HashMap<>();
        if (status == null || status.getAddresses() == null) return m;
        for (NodeAddress a : status.getAddresses()) {
            if (a == null) continue;
            if (a.getType() != null && a.getAddress() != null) {
                m.put(a.getType(), a.getAddress());
            }
        }
        return m;
    }

    private static String nullSafe(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
