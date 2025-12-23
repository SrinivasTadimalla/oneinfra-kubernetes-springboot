package com.srikar.kubernetes.service;

import com.srikar.kubernetes.dto.ConfigMapDtos.ConfigMapDetail;
import com.srikar.kubernetes.dto.ConfigMapDtos.ConfigMapSummary;
import com.srikar.kubernetes.dto.ConfigMapDtos.UpsertConfigMap;
import com.srikar.kubernetes.utilities.Helper;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ConfigMapService {

    private final KubernetesClient client;

    public ConfigMapService(KubernetesClient client) {
        this.client = client;
    }

    public List<ConfigMapSummary> list(String namespace) {
        return client.configMaps()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .map(cm -> new ConfigMapSummary(
                        cm.getMetadata().getName(),
                        (cm.getData() == null) ? 0 : cm.getData().size()
                ))
                .toList();
    }

    public ConfigMapDetail get(String namespace, String name) {
        ConfigMap cm = client.configMaps()
                .inNamespace(namespace)
                .withName(name)
                .get();

        if (cm == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ConfigMap not found");
        }

        return toDetail(cm);
    }

    public ConfigMapDetail upsert(UpsertConfigMap req) {
        String ns = req.getNamespace();
        String name = req.getName();

        ConfigMap desired = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(name)
                .withNamespace(ns)
                .withLabels(Helper.nullToEmptyMap(req.getLabels()))
                .endMetadata()
                .withData(Helper.nullToEmptyMap(req.getData()))
                .build();

        ConfigMap saved = client.configMaps()
                .inNamespace(ns)
                .resource(desired)
                .createOrReplace(); // warning: deprecated in newer Fabric8, but works

        return toDetail(saved);
    }

    public void delete(String namespace, String name) {
        List<StatusDetails> result = client.configMaps()
                .inNamespace(namespace)
                .withName(name)
                .delete();

        if (result == null || result.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "ConfigMap not found or already deleted"
            );
        }
    }

    private static ConfigMapDetail toDetail(ConfigMap cm) {
        String ts = (cm.getMetadata() != null) ? cm.getMetadata().getCreationTimestamp() : null;
        Instant created = Helper.parseK8sCreationTimestamp(ts);

        Map<String, String> data = Helper.nullToEmptyMap(cm.getData());

        Map<String, String> labels =
                (cm.getMetadata() != null)
                        ? Helper.nullToEmptyMap(cm.getMetadata().getLabels())
                        : Map.of();

        return new ConfigMapDetail(
                cm.getMetadata().getName(),
                cm.getMetadata().getNamespace(),
                data,
                labels,
                created
        );
    }
}
