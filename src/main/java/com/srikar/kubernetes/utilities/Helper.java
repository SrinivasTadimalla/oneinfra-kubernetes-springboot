package com.srikar.kubernetes.utilities;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.net.InetAddress;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Helper {

    private Helper() {}

    /** Lightweight health probe for Kubernetes connectivity */
    public static boolean isClusterHealthy(KubernetesClient client) {
        try {
            client.namespaces().list();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Extract and sort namespace names */
    public static List<String> extractNamespaceNames(List<Namespace> namespaces) {
        return namespaces.stream()
                .map(ns -> ns.getMetadata().getName())
                .sorted()
                .toList();
    }

    /** Parse RFC3339 / ISO timestamps safely to Instant */
    public static Instant parseToInstant(String rfc3339) {
        if (rfc3339 == null || rfc3339.isBlank()) return null;
        try {
            return OffsetDateTime.parse(rfc3339).toInstant();
        } catch (DateTimeParseException e) {
            try {
                return Instant.parse(rfc3339);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    /** Kubernetes creationTimestamp → Instant */
    public static Instant parseK8sCreationTimestamp(String creationTs) {
        return parseToInstant(creationTs);
    }

    /** Format age similar to kubectl: 10m, 2h, 5d */
    public static String fmtAge(String creationTs) {
        if (creationTs == null || creationTs.isBlank()) return "—";
        try {
            Instant created = OffsetDateTime.parse(creationTs).toInstant();
            long mins = created.until(Instant.now(), ChronoUnit.MINUTES);
            if (mins < 60) return mins + "m";
            long hours = mins / 60;
            if (hours < 48) return hours + "h";
            return (hours / 24) + "d";
        } catch (Exception e) {
            return "—";
        }
    }

    /** Null/blank safe fallback */
    public static String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    /** Null-safe Map → empty Map */
    public static <K, V> Map<K, V> nullToEmptyMap(Map<K, V> map) {
        return (map == null) ? Map.of() : map;
    }

    /** Build a human-readable ports string, including targetPort and NodePort */
    public static String fmtPorts(Service svc) {
        var spec = svc.getSpec();
        List<ServicePort> ports = spec.getPorts();
        if (ports == null || ports.isEmpty()) return "—";

        boolean showNodePort =
                "NodePort".equalsIgnoreCase(spec.getType()) ||
                        "LoadBalancer".equalsIgnoreCase(spec.getType());

        return ports.stream()
                .map(p -> {
                    String proto   = p.getProtocol() == null ? "TCP" : p.getProtocol();
                    String portStr = (p.getPort() != null ? p.getPort() : 0) + "/" + proto;

                    String target = null;
                    if (p.getTargetPort() != null) {
                        if (p.getTargetPort().getIntVal() != null) {
                            target = String.valueOf(p.getTargetPort().getIntVal());
                        } else if (p.getTargetPort().getStrVal() != null
                                && !p.getTargetPort().getStrVal().isBlank()) {
                            target = p.getTargetPort().getStrVal();
                        }
                    }

                    String arrow = (target != null) ? " → " + target : "";
                    String node  = (showNodePort && p.getNodePort() != null)
                            ? " • NodePort " + p.getNodePort()
                            : "";

                    return portStr + arrow + node;
                })
                .collect(Collectors.joining(", "));
    }

    /** Remove server-managed fields so Deployment YAML is re-applicable */
    public static void sanitizeDeploymentForYaml(Deployment d) {
        if (d == null) return;

        ObjectMeta md = d.getMetadata();
        if (md != null) {
            md.setManagedFields(null);
            md.setUid(null);
            md.setResourceVersion(null);
            md.setGeneration(null);
            md.setCreationTimestamp(null);
        }
        d.setStatus(null);
    }

    /** Convert IP String -> InetAddress (for Postgres inet columns) */
    public static InetAddress toInet(String ip) {
        if (ip == null || ip.isBlank()) return null;
        try {
            return InetAddress.getByName(ip.trim());
        } catch (Exception e) {
            return null; // keep it lenient; you can throw if you want strict
        }
    }

}
