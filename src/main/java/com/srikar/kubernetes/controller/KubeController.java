package com.srikar.kubernetes.controller;

import com.srikar.kubernetes.dto.PodStatus;
import com.srikar.kubernetes.service.KubeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/k8s")
public class KubeController {

    private final KubeService kube;

    public KubeController(KubeService kube) {
        this.kube = kube;
    }

    /** Liveness probe */
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    /** Kubernetes connectivity diagnostics */
    @GetMapping(value = "/diag", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> diag() {
        if (kube.isHealthy()) {
            return ResponseEntity.ok("cluster-ok");
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("cluster-unreachable");
    }

    /** List namespaces */
    @GetMapping(value = "/namespaces", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> namespaces() {
        List<String> namespaces = kube.listNamespaces();
        return ResponseEntity.ok(namespaces);
    }

    /** List pods in a namespace */
    @GetMapping(value = "/pods/{namespace}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PodStatus>> pods(@PathVariable String namespace) {
        List<PodStatus> pods = kube.listPods(namespace);
        return ResponseEntity.ok(pods);
    }
}
