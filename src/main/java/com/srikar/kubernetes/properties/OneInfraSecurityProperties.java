package com.srikar.kubernetes.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "oneinfra.security")
public class OneInfraSecurityProperties {

    private Keycloak keycloak = new Keycloak();

    @Getter
    @Setter
    public static class Keycloak {
        private String clientId;
    }
}
