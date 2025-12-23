package com.srikar.kubernetes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.Map;

public final class ConfigMapDtos {

    private ConfigMapDtos() {
        // utility holder
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfigMapSummary {
        private String name;
        private int keyCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfigMapDetail {
        private String name;
        private String namespace;
        private Map<String, String> data;
        private Map<String, String> labels;
        private Instant creationTimestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpsertConfigMap {

        @NotBlank(message = "name must not be blank")
        private String name;

        @NotBlank(message = "namespace must not be blank")
        private String namespace;

        @NotNull(message = "data must not be null")
        private Map<String, String> data;

        private Map<String, String> labels;
    }

}