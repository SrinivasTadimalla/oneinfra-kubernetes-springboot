package com.srikar.kubernetes.dto;

import com.srikar.kubernetes.dto.PodStatus;
import io.fabric8.kubernetes.api.model.Pod;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public final class PodMapper {

    private PodMapper() {}

    public static PodStatus toDto(Pod pod) {
        if (pod == null) return new PodStatus(null, null, null, null, null, null);

        var meta   = pod.getMetadata();
        var spec   = pod.getSpec();
        var status = pod.getStatus();

        String name      = meta != null ? meta.getName() : null;
        String namespace = meta != null ? meta.getNamespace() : null;
        String phase     = status != null ? status.getPhase() : null;
        String nodeName  = spec != null ? spec.getNodeName() : null;
        String podIP     = status != null ? status.getPodIP() : null;

        String startTimeStr = (status != null) ? status.getStartTime() : null;
        Instant startTime = parseToInstant(startTimeStr);

        return new PodStatus(name, namespace, phase, nodeName, podIP, startTime);
    }

    private static Instant parseToInstant(String rfc3339) {
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
}
