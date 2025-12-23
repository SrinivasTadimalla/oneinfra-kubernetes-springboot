package com.srikar.kubernetes.controller;

import com.srikar.kubernetes.dto.ConfigMapDtos;
import com.srikar.kubernetes.dto.ConfigMapDtos.ConfigMapDetail;
import com.srikar.kubernetes.dto.ConfigMapDtos.UpsertConfigMap;
import com.srikar.kubernetes.service.ConfigMapService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/k8s")
public class ConfigMapController {

    private final ConfigMapService cfg;

    public ConfigMapController(ConfigMapService cfg) {
        this.cfg = cfg;
    }

    @GetMapping(value = "/configmaps/{namespace}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConfigMapDtos.ConfigMapSummary> list(@PathVariable String namespace) {
        return cfg.list(namespace);
    }

    @GetMapping(value = "/configmaps/{namespace}/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigMapDetail get(@PathVariable String namespace, @PathVariable String name) {
        return cfg.get(namespace, name);
    }

    @PostMapping(
            value = "/configmaps",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ConfigMapDetail> upsert(@RequestBody @Valid UpsertConfigMap req) {
        ConfigMapDetail detail = cfg.upsert(req);

        URI location = URI.create(String.format(
                "/k8s/configmaps/%s/%s",
                detail.getNamespace(),
                detail.getName()
        ));

        // 201 Created + Location header + body
        return ResponseEntity.created(location).body(detail);
    }

    @DeleteMapping("/configmaps/{namespace}/{name}")
    public ResponseEntity<Void> delete(@PathVariable String namespace, @PathVariable String name) {
        cfg.delete(namespace, name);
        return ResponseEntity.noContent().build();
    }
}
