package com.srikar.kubernetes.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class ClusterDto {
    UUID id;
    String name;
    Instant createdAt;
    Instant updatedAt;
    List<ClusterNodeDto> nodes;
}
