package com.srikar.kubernetes.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;

public final class KeycloakJwtAuthConverter {

    private KeycloakJwtAuthConverter() {}

    /**
     * Build a JwtAuthenticationConverter that maps Keycloak roles to Spring authorities:
     * - realm roles:    realm_access.roles  -> ROLE_<role>
     * - client roles:   resource_access.<clientId>.roles -> ROLE_<role>
     */
    public static JwtAuthenticationConverter build(String clientIdForClientRoles) {

        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthorityPrefix("SCOPE_"); // keep scopes too (optional)

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();

            // Include scopes (optional but harmless)
            Collection<GrantedAuthority> scopes = scopeConverter.convert(jwt);
            if (scopes != null) authorities.addAll(scopes);

            // Realm roles: realm_access.roles
            authorities.addAll(extractRealmRoles(jwt));

            // Client roles: resource_access.<clientId>.roles
            if (clientIdForClientRoles != null && !clientIdForClientRoles.isBlank()) {
                authorities.addAll(extractClientRoles(jwt, clientIdForClientRoles));
            }

            return authorities;
        });

        return converter;
    }

    private static Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (!(realmAccess instanceof Map<?, ?> ra)) return List.of();

        Object rolesObj = ra.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) return List.of();

        List<GrantedAuthority> out = new ArrayList<>();
        for (Object r : roles) {
            if (r != null) out.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
        }
        return out;
    }

    private static Collection<GrantedAuthority> extractClientRoles(Jwt jwt, String clientId) {
        Object resourceAccess = jwt.getClaim("resource_access");
        if (!(resourceAccess instanceof Map<?, ?> res)) return List.of();

        Object client = res.get(clientId);
        if (!(client instanceof Map<?, ?> ca)) return List.of();

        Object rolesObj = ca.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) return List.of();

        List<GrantedAuthority> out = new ArrayList<>();
        for (Object r : roles) {
            if (r != null) out.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
        }
        return out;
    }
}
