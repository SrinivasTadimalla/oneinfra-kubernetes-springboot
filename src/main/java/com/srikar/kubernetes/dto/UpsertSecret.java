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
public class UpsertSecret {
    private String name;
    private String namespace;
    private String type;                        // optional, default "Opaque"
    private Map<String, String> data;           // plaintext (to be base64-encoded)
    private Map<String, String> labels;
}
