package com.srikar.kubernetes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PodStatus {
    private String name;
    private String namespace;
    private String phase;
    private String nodeName;
    private String podIP;
    private Instant startTime;
}
