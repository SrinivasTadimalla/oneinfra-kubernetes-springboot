package com.srikar.kubernetes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngressSummary {

    private String name;

    private String host;

    private boolean tls;

    @JsonProperty("class")
    private String clazz;

    private List<String> paths;

    private String age;

}
