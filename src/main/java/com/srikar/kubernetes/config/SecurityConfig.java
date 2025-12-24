package com.srikar.kubernetes.config;

import com.srikar.kubernetes.properties.OneInfraSecurityProperties;
import com.srikar.kubernetes.security.KeycloakJwtAuthConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
        String clientId = (props.getKeycloak() != null) ? props.getKeycloak().getClientId() : null;
        var jwtAuthConverter = KeycloakJwtAuthConverter.build(clientId);

        http
                // ✅ IMPORTANT: Spring Security must enable CORS for browser preflight (OPTIONS)
                .cors(Customizer.withDefaults())

                // If you're using stateless JWT, CSRF can stay disabled
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // ✅ Let browser preflight through (critical for Angular)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints
                        .requestMatchers("/k8s/health").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Everything else requires JWT
                        .anyRequest().authenticated()
                )

                // ✅ Keycloak JWT resource server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        return http.build();
    }

    /**
     * ✅ CORS policy used by Spring Security (preflight + actual requests).
     * This is the most reliable way to fix:
     * "No 'Access-Control-Allow-Origin' header is present..."
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration cfg = new CorsConfiguration();

        // Angular dev origins
        cfg.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://192.168.66.108:4200",
                "http://192.168.66.115:4200"
        ));

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization", "Location"));
        cfg.setAllowCredentials(true);

        // cache preflight for 1 hour
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
