package com.srikar.kubernetes.config;

import com.srikar.kubernetes.properties.OneInfraSecurityProperties;
import com.srikar.kubernetes.security.KeycloakJwtAuthConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(OneInfraSecurityProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OneInfraSecurityProperties props
    ) throws Exception {

        // ✅ read clientId from: oneinfra.security.keycloak.client-id
        String clientId = null;
        if (props.getKeycloak() != null) {
            clientId = props.getKeycloak().getClientId();
        }

        var jwtAuthConverter = KeycloakJwtAuthConverter.build(clientId);

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/k8s/health").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        return http.build();
    }
}
