package com.srikar.kubernetes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSummary {

    private String name;
    private String type;
    private String clusterIP;
    private String ports;   // e.g. "9009/TCP → 9009 • NodePort 30909"
    private String age;

}
