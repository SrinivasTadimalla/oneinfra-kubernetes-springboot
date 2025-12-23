package com.srikar.kubernetes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertConfigMap {
    private String name;
    private String namespace;
    private Map<String, String> data;
    private Map<String, String> labels;
}
