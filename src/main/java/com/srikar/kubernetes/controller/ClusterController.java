package com.srikar.kubernetes.controller;

import com.srikar.kubernetes.api.ApiResponse;
import com.srikar.kubernetes.dto.ClusterDto;
import com.srikar.kubernetes.entity.ClusterEntity;
import com.srikar.kubernetes.service.ClusterInventoryService;
import com.srikar.kubernetes.utilities.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/k8s")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterInventoryService inventory;

    /**
     * WRITE operation
     * Only KUBERNETES_ADMIN can upsert cluster + nodes snapshot
     */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @PutMapping("/clusters/{clusterName}")
    public ResponseEntity<?> upsert(@PathVariable String clusterName) {
        ClusterEntity c = inventory.upsertClusterFromK8s(clusterName);
        return ResponseEntity.ok(
                Map.of(
                        "cluster", c.getName(),
                        "updatedAt", c.getUpdatedAt().toString()
                )
        );
    }

    /**
     * READ operation
     * DEV / TEST / ADMIN can list clusters
     *
     * Standardized API response wrapper
     * (same response contract as Kafka microservice)
     */
    @PreAuthorize("hasAnyRole('KUBERNETES_ADMIN','KUBERNETES_DEV','KUBERNETES_TEST')")
    @GetMapping("/clusters")
    public ResponseEntity<ApiResponse<List<ClusterDto>>> listClusters() {

        List<ClusterDto> clusters = inventory.getClusterDtos();

        return ResponseEntity.ok(
                ApiResponses.ok("Clusters fetched successfully", clusters)
        );
    }
}
