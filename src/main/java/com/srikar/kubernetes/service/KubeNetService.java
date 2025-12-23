package com.srikar.kubernetes.service;

import com.srikar.kubernetes.dto.IngressSummary;
import com.srikar.kubernetes.dto.ServiceSummary;
import com.srikar.kubernetes.utilities.Helper;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class KubeNetService {

    private static final String DASH = "—";

    private final KubernetesClient client;

    public KubeNetService(KubernetesClient client) {
        this.client = client;
    }

    /** List Services in a namespace */
    public List<ServiceSummary> listServices(String namespace) {
        return client.services()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .map(svc -> {
                    var md = svc.getMetadata();
                    var spec = svc.getSpec();

                    String name = (md != null) ? md.getName() : null;

                    String clusterIpStr =
                            (spec != null && spec.getClusterIPs() != null && !spec.getClusterIPs().isEmpty())
                                    ? String.join(", ", spec.getClusterIPs())
                                    : Helper.nullToDash(spec != null ? spec.getClusterIP() : null);

                    String type =
                            (spec == null || spec.getType() == null || spec.getType().isBlank())
                                    ? "ClusterIP"
                                    : spec.getType();

                    String age = (md != null) ? Helper.fmtAge(md.getCreationTimestamp()) : DASH;

                    return ServiceSummary.builder()
                            .name(name)
                            .type(type)
                            .clusterIP(clusterIpStr)
                            .ports(Helper.fmtPorts(svc))
                            .age(age)
                            .build();
                })
                .toList();
    }

    /** List Ingress objects in a namespace */
    public List<IngressSummary> listIngress(String namespace) {
        return client.network()
                .v1()
                .ingresses()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .flatMap(ing -> {
                    var md = ing.getMetadata();
                    var spec = ing.getSpec();

                    String name = (md != null) ? md.getName() : null;
                    String age = (md != null) ? Helper.fmtAge(md.getCreationTimestamp()) : DASH;

                    boolean tls = spec != null && spec.getTls() != null && !spec.getTls().isEmpty();
                    String clazz = (spec != null && spec.getIngressClassName() != null)
                            ? spec.getIngressClassName()
                            : DASH;

                    var rules = (spec != null) ? spec.getRules() : null;

                    if (rules == null || rules.isEmpty()) {
                        return Stream.of(buildIngress(name, DASH, tls, clazz, List.of("/"), age));
                    }

                    return rules.stream().map(r -> {
                        String host = (r.getHost() != null) ? r.getHost() : DASH;

                        List<String> paths =
                                (r.getHttp() != null && r.getHttp().getPaths() != null)
                                        ? r.getHttp().getPaths().stream()
                                        .map(p -> p.getPath() != null ? p.getPath() : "/")
                                        .toList()
                                        : List.of("/");

                        return buildIngress(name, host, tls, clazz, paths, age);
                    });
                })
                .toList();
    }

    private static IngressSummary buildIngress(
            String name,
            String host,
            boolean tls,
            String clazz,
            List<String> paths,
            String age
    ) {
        return IngressSummary.builder()
                .name(name)
                .host(host)
                .tls(tls)
                .clazz(clazz)
                .paths(paths)
                .age(age)
                .build();
    }
}
