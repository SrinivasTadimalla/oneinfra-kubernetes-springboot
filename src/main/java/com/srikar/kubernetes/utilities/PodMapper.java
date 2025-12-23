package com.srikar.kubernetes.utilities;

import com.srikar.kubernetes.dto.PodStatus;
import io.fabric8.kubernetes.api.model.Pod;

public final class PodMapper {

    private PodMapper() {}

    public static PodStatus toDto(Pod pod) {
        if (pod == null) {
            return PodStatus.builder().build();
        }

        var meta   = pod.getMetadata();
        var spec   = pod.getSpec();
        var status = pod.getStatus();

        return PodStatus.builder()
                .name(meta != null ? meta.getName() : null)
                .namespace(meta != null ? meta.getNamespace() : null)
                .phase(status != null ? status.getPhase() : null)
                .nodeName(spec != null ? spec.getNodeName() : null)
                .podIP(status != null ? status.getPodIP() : null)
                .startTime(
                        Helper.parseToInstant(
                                status != null ? status.getStartTime() : null
                        )
                )
                .build();
    }
}
