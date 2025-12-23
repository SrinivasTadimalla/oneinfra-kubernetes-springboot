package com.srikar.kubernetes.service;

import com.srikar.kubernetes.utilities.Helper;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DeploymentService {

    private final KubernetesClient client;

    public DeploymentService(KubernetesClient client) {
        this.client = client;
    }

    public List<String> list(String namespace) {
        return client.apps().deployments()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .map(d -> d.getMetadata().getName())
                .toList();
    }

    public String getAsYaml(String namespace, String name) {
        Deployment d = client.apps().deployments()
                .inNamespace(namespace)
                .withName(name)
                .get();

        if (d == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Deployment not found");
        }

        Helper.sanitizeDeploymentForYaml(d);
        return Serialization.asYaml(d);
    }

    public Deployment createFromYaml(String namespace, String yaml) {
        try (ByteArrayInputStream in =
                     new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))) {

            Deployment created = client.apps().deployments()
                    .inNamespace(namespace)
                    .load(in)
                    .create();

            Helper.sanitizeDeploymentForYaml(created);
            return created;

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid deployment YAML or create failed: " + e.getMessage(),
                    e
            );
        }
    }

    public Deployment upsertFromYaml(String namespace, String name, String yaml) {
        Deployment d;
        try {
            d = Serialization.unmarshal(yaml, Deployment.class);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid deployment YAML: " + e.getMessage(),
                    e
            );
        }

        if (d.getMetadata() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deployment YAML missing metadata");
        }

        d.getMetadata().setNamespace(namespace);
        d.getMetadata().setName(name);

        Helper.sanitizeDeploymentForYaml(d);

        try {
            Deployment saved = client.apps().deployments()
                    .inNamespace(namespace)
                    .resource(d)
                    .createOrReplace();

            Helper.sanitizeDeploymentForYaml(saved);
            return saved;

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Upsert failed: " + e.getMessage(),
                    e
            );
        }
    }

    public boolean delete(String namespace, String name) {
        Resource<Deployment> res = client.apps().deployments()
                .inNamespace(namespace)
                .withName(name);

        if (res.get() == null) return false;

        var details = res.delete();
        return details != null && !details.isEmpty();
    }
}
