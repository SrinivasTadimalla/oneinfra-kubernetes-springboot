package com.srikar.kubernetes.controller;

import com.srikar.kubernetes.service.DeploymentService;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.utils.Serialization;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/k8s")
public class DeploymentController {

    private static final MediaType TEXT_YAML = MediaType.valueOf("text/yaml");

    private final DeploymentService svc;

    public DeploymentController(DeploymentService svc) {
        this.svc = svc;
    }

    /** a) List all Deployments (READ) */
    @PreAuthorize("hasAnyRole('KUBERNETES_ADMIN','KUBERNETES_DEV','KUBERNETES_TEST')")
    @GetMapping(value = "/deployments/{namespace}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> list(@PathVariable @NotBlank String namespace) {
        return svc.list(namespace);
    }

    /** b) Get deployment YAML (READ) */
    @PreAuthorize("hasAnyRole('KUBERNETES_ADMIN','KUBERNETES_DEV','KUBERNETES_TEST')")
    @GetMapping(value = "/deployments/{namespace}/{name}/yaml", produces = "text/yaml")
    public ResponseEntity<String> getYaml(@PathVariable String namespace,
                                          @PathVariable String name) {
        String yaml = svc.getAsYaml(namespace, name);
        return ResponseEntity.ok()
                .contentType(TEXT_YAML)
                .body(yaml);
    }

    /** c) Create Deployment from YAML (WRITE) */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @PostMapping(
            value = "/deployments/{namespace}/yaml",
            consumes = {"text/yaml", "application/yaml"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> create(@PathVariable String namespace,
                                                      @RequestBody String yaml) {
        Deployment created = svc.createFromYaml(namespace, yaml);

        String createdName = created.getMetadata() != null ? created.getMetadata().getName() : null;

        URI loc = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .replacePath("/k8s/deployments/{ns}/{name}/yaml")
                .buildAndExpand(namespace, createdName)
                .toUri();

        Map<String, String> body = Map.of(
                "namespace", namespace,
                "name", createdName
        );

        return ResponseEntity.created(loc).body(body);
    }

    /** d) Upsert Deployment from YAML (WRITE) */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @PutMapping(
            value = "/deployments/{namespace}/{name}/yaml",
            consumes = {"text/yaml", "application/yaml"},
            produces = "text/yaml"
    )
    public ResponseEntity<String> update(@PathVariable String namespace,
                                         @PathVariable String name,
                                         @RequestBody String yaml) {
        Deployment updated = svc.upsertFromYaml(namespace, name, yaml);

        return ResponseEntity.ok()
                .contentType(TEXT_YAML)
                .body(Serialization.asYaml(updated));
    }

    /** e) Delete Deployment (WRITE) */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @DeleteMapping("/deployments/{namespace}/{name}")
    public ResponseEntity<Void> delete(@PathVariable String namespace,
                                       @PathVariable String name) {
        boolean deleted = svc.delete(namespace, name);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
