package com.srikar.kubernetes.service;

import com.srikar.kubernetes.dto.PodStatus;
import com.srikar.kubernetes.utilities.PodMapper;
import com.srikar.kubernetes.utilities.Helper;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KubeService {

    private final KubernetesClient client;

    public KubeService(KubernetesClient client) {
        this.client = client;
    }

    public boolean isHealthy() {
        return Helper.isClusterHealthy(client);
    }

    public List<String> listNamespaces() {
        return Helper.extractNamespaceNames(
                client.namespaces()
                        .list()
                        .getItems()
        );
    }

    /** Return PodStatus DTOs expected by the Angular UI */
    public List<PodStatus> listPods(String namespace) {
        return client.pods()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .map(PodMapper::toDto)
                .toList();
    }
}
