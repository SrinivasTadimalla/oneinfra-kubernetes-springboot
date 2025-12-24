package com.srikar.kubernetes.controller;

import com.srikar.kubernetes.dto.SecretDetail;
import com.srikar.kubernetes.dto.SecretSummary;
import com.srikar.kubernetes.dto.UpsertSecret;
import com.srikar.kubernetes.service.SecretService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/k8s")
public class SecretController {

    private static final MediaType TEXT_YAML = MediaType.valueOf("text/yaml");

    private final SecretService svc;

    public SecretController(SecretService svc) {
        this.svc = svc;
    }

    /** List secrets (summary, no values). (READ) */
    @PreAuthorize("hasAnyRole('KUBERNETES_ADMIN','KUBERNETES_DEV','KUBERNETES_TEST')")
    @GetMapping(value = "/secrets/{namespace}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SecretSummary>> list(@PathVariable String namespace) {
        return ResponseEntity.ok(svc.list(namespace));
    }

    /** Secret detail (plaintext values). (ADMIN only) */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @GetMapping(value = "/secrets/{namespace}/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SecretDetail> get(@PathVariable String namespace,
                                            @PathVariable String name) {
        SecretDetail d = svc.get(namespace, name);
        return (d != null) ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
    }

    /** Create from plaintext. (ADMIN only) */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @PostMapping(value = "/secrets",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SecretDetail> create(@RequestBody UpsertSecret req) {
        SecretDetail created = svc.create(req);

        URI loc = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{namespace}/{name}")
                .buildAndExpand(created.getNamespace(), created.getName())
                .toUri();

        return ResponseEntity.created(loc).body(created);
    }

    /** Update from plaintext. (ADMIN only) */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @PutMapping(value = "/secrets/{namespace}/{name}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> update(@PathVariable String namespace,
                                                      @PathVariable String name,
                                                      @RequestBody UpsertSecret req) {
        boolean ok = svc.update(namespace, name, req);
        return ok ? ResponseEntity.ok(Map.of("updated", true))
                : ResponseEntity.notFound().build();
    }

    /** Delete. (ADMIN only) */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @DeleteMapping("/secrets/{namespace}/{name}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String namespace,
                                                      @PathVariable String name) {
        boolean ok = svc.delete(namespace, name);
        return ok ? ResponseEntity.ok(Map.of("deleted", true))
                : ResponseEntity.notFound().build();
    }

    /** Download as YAML (values base64). (ADMIN only) */
    @PreAuthorize("hasRole('KUBERNETES_ADMIN')")
    @GetMapping(value = "/secrets/{namespace}/{name}/yaml", produces = "text/yaml")
    public ResponseEntity<String> yaml(@PathVariable String namespace,
                                       @PathVariable String name) {
        String y = svc.asYaml(namespace, name);
        return (y != null)
                ? ResponseEntity.ok().contentType(TEXT_YAML).body(y)
                : ResponseEntity.notFound().build();
    }
}
