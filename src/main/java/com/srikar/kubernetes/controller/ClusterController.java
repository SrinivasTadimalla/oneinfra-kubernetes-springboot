package com.srikar.kubernetes.controller;

import com.srikar.kubernetes.entity.ClusterEntity;
import com.srikar.kubernetes.service.ClusterInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/k8s")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterInventoryService inventory;

    @PostMapping("/clusters/{clusterName}/refresh")
    public ResponseEntity<?> refresh(@PathVariable String clusterName) {
        ClusterEntity c = inventory.refreshCluster(clusterName);
        return ResponseEntity.ok().body(
                java.util.Map.of("cluster", c.getName(), "updatedAt", c.getUpdatedAt().toString())
        );
    }
}
