package com.srikar.kubernetes.service;

import com.srikar.kubernetes.dto.SecretDetail;
import com.srikar.kubernetes.dto.SecretSummary;
import com.srikar.kubernetes.dto.UpsertSecret;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SecretService {

    private final KubernetesClient client;

    public SecretService(KubernetesClient client) {
        this.client = client;
    }

    // ---- Helpers ------------------------------------------------------------

    private static Map<String, String> b64Encode(Map<String, String> plain) {
        if (plain == null) return Map.of();
        Base64.Encoder enc = Base64.getEncoder();
        Map<String, String> out = new LinkedHashMap<>();
        plain.forEach((k, v) ->
                out.put(k, enc.encodeToString((v != null ? v : "").getBytes(StandardCharsets.UTF_8))));
        return out;
    }

    private static Map<String, String> b64Decode(Map<String, String> b64) {
        if (b64 == null) return Map.of();
        Base64.Decoder dec = Base64.getDecoder();
        Map<String, String> out = new LinkedHashMap<>();
        b64.forEach((k, v) -> {
            try {
                out.put(k, new String(dec.decode(v != null ? v : ""), StandardCharsets.UTF_8));
            } catch (IllegalArgumentException e) {
                // if the value isn't valid b64, pass it through as-is
                out.put(k, v != null ? v : "");
            }
        });
        return out;
    }

    private static void scrubServerFields(Secret s) {
        if (s.getMetadata() != null) {
            s.getMetadata().setManagedFields(null);
            s.getMetadata().setUid(null);
            s.getMetadata().setResourceVersion(null);
            s.getMetadata().setCreationTimestamp(null);
            s.getMetadata().setGeneration(null);
        }
    }

    private static String defaultType(String type) {
        return (type == null || type.isBlank()) ? "Opaque" : type;
    }

    // ---- Operations ---------------------------------------------------------

    /** List secrets (no values). */
    public List<SecretSummary> list(String namespace) {
        return client.secrets()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .map(s -> SecretSummary.builder()
                        .name(s.getMetadata().getName())
                        .type(s.getType())
                        .keyCount(s.getData() != null ? s.getData().size() : 0)
                        .creationTimestamp(s.getMetadata().getCreationTimestamp())
                        .build())
                .toList();
    }

    /** Detail (plaintext values). Returns null if not found (controller can map to 404). */
    public SecretDetail get(String namespace, String name) {
        Secret s = client.secrets().inNamespace(namespace).withName(name).get();
        if (s == null) return null;

        ObjectMeta m = s.getMetadata();
        return SecretDetail.builder()
                .name(m.getName())
                .namespace(m.getNamespace())
                .type(s.getType())
                .data(b64Decode(s.getData()))
                .labels(m.getLabels() != null ? m.getLabels() : Map.of())
                .creationTimestamp(m.getCreationTimestamp())
                .build();
    }

    /** Create from plaintext. */
    public SecretDetail create(UpsertSecret req) {
        Secret s = new Secret();
        s.setType(defaultType(req.getType()));

        ObjectMeta m = new ObjectMeta();
        m.setName(req.getName());
        m.setNamespace(req.getNamespace());
        if (req.getLabels() != null) m.setLabels(req.getLabels());
        s.setMetadata(m);

        s.setData(b64Encode(req.getData()));

        Secret created = client.secrets()
                .inNamespace(req.getNamespace())
                .resource(s)
                .create();

        ObjectMeta cm = created.getMetadata();
        return SecretDetail.builder()
                .name(cm.getName())
                .namespace(cm.getNamespace())
                .type(created.getType())
                .data(b64Decode(created.getData()))
                .labels(cm.getLabels() != null ? cm.getLabels() : Map.of())
                .creationTimestamp(cm.getCreationTimestamp())
                .build();
    }

    /** Update (createOrReplace) from plaintext. Returns false if not found. */
    public boolean update(String namespace, String name, UpsertSecret req) {
        Secret existing = client.secrets().inNamespace(namespace).withName(name).get();
        if (existing == null) return false;

        Secret s = new Secret();

        String typeToUse =
                (req.getType() == null || req.getType().isBlank())
                        ? (existing.getType() != null ? existing.getType() : "Opaque")
                        : req.getType();
        s.setType(typeToUse);

        ObjectMeta m = new ObjectMeta();
        m.setName(name);
        m.setNamespace(namespace);
        m.setLabels(req.getLabels() != null ? req.getLabels() : existing.getMetadata().getLabels());
        s.setMetadata(m);

        s.setData(b64Encode(req.getData()));

        client.secrets().inNamespace(namespace).resource(s).createOrReplace();
        return true;
    }

    /** Delete by name. */
    public boolean delete(String namespace, String name) {
        var res = client.secrets().inNamespace(namespace).withName(name);
        if (res.get() == null) return false;
        var details = res.delete();
        return details != null && !details.isEmpty();
    }

    /** YAML (values base64). */
    public String asYaml(String namespace, String name) {
        Secret s = client.secrets().inNamespace(namespace).withName(name).get();
        if (s == null) return null;

        // Leave s.getData() as base64; scrub server-set fields for portability
        scrubServerFields(s);
        return Serialization.asYaml(s);
    }
}
