package com.srikar.kubernetes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecretSummary {
    private String name;
    private String type;
    private int keyCount;
    private String creationTimestamp;
}
