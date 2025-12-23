package com.srikar.kubernetes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRow {

    private String name;
    private String type;
    private String clusterIP;
    private String ports;   // e.g. "9009/TCP → 9009 • NodePort 30909"
    private String age;
}
