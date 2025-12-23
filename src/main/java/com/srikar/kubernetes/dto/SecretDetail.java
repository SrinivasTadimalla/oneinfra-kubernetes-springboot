package com.srikar.kubernetes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecretDetail {
    private String name;
    private String namespace;
    private String type;
    private Map<String, String> data;           // plaintext (decoded)
    private Map<String, String> labels;
    private String creationTimestamp;
}
