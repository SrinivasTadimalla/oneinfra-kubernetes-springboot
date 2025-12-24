package com.srikar.kubernetes.dto;

import lombok.Builder;
import lombok.Value;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class ClusterNodeDto {
    UUID id;
    String nodeName;
    String status;
    String roles;
    String kubeVersion;
    InetAddress internalIp;
    InetAddress externalIp;
    Boolean isControlPlane;
    Boolean isVm;
    String vmName;
    Instant observedAt;
}
