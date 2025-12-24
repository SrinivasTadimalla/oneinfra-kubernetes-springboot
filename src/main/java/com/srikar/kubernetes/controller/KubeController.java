package com.srikar.kubernetes.controller;

import com.srikar.kubernetes.api.ApiResponse;
import com.srikar.kubernetes.dto.PodStatus;
import com.srikar.kubernetes.service.KubeService;
import com.srikar.kubernetes.utilities.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/k8s")
public class KubeController {

    private final KubeService kube;

    public KubeController(KubeService kube) {
        this.kube = kube;
    }

    /**
     * Liveness probe
     * Exposed for LB / Kubernetes health checks
     */
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    /**
     * Kubernetes connectivity diagnostics
     * ADMIN only (infra visibility)
     */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @GetMapping(value = "/diag", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> diag() {
        if (kube.isHealthy()) {
            return ResponseEntity.ok("cluster-ok");
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("cluster-unreachable");
    }

    /**
     * List namespaces (READ)
     * Standardized API envelope (consistent with /k8s/clusters)
     */
    @PreAuthorize("hasAnyRole('KUBERNETES_ADMIN','KUBERNETES_DEV','KUBERNETES_TEST')")
    @GetMapping(value = "/namespaces", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> namespaces() {
        List<String> namespaces = kube.listNamespaces();
        return ResponseEntity.ok(ApiResponses.ok("Namespaces fetched successfully", namespaces));
    }

    /**
     * List pods in a namespace (READ)
     * Standardized API envelope (consistent with /k8s/clusters)
     */
    @PreAuthorize("hasAnyRole('KUBERNETES_ADMIN','KUBERNETES_DEV','KUBERNETES_TEST')")
    @GetMapping(value = "/pods/{namespace}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<PodStatus>>> pods(@PathVariable String namespace) {
        List<PodStatus> pods = kube.listPods(namespace);
        return ResponseEntity.ok(ApiResponses.ok("Pods fetched successfully", pods));
    }
}
