package com.acs_tr069.test_tr069.Security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String RESOURCE_ACCESS = "resource_access";
    private static final String GROUPS = "groups";

    private final String clientId;

    public CustomJwtAuthenticationConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Realm roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            authorities.addAll(roles.stream()
                    .map(role -> {
                        String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        return new SimpleGrantedAuthority(roleName);
                    })
                    .collect(Collectors.toSet()));
        }

        // 2. Client (resource) roles
        Map<String, Object> resourceAccess = jwt.getClaimAsMap(RESOURCE_ACCESS);
        if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
            Map<String, Object> clientRoles = (Map<String, Object>) resourceAccess.get(clientId);
            if (clientRoles != null && clientRoles.containsKey("roles")) {
                List<String> roles = (List<String>) clientRoles.get("roles");
                authorities.addAll(roles.stream()
                        .map(role -> {
                        String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        return new SimpleGrantedAuthority(roleName);
                    })
                        .collect(Collectors.toSet()));
            }
        }

        // 3. Groups
        List<String> groups = jwt.getClaimAsStringList(GROUPS);
        if (groups != null) {
            authorities.addAll(groups.stream()
                    .map(group -> {
                        String groupName = group.replace("/", "");
                        groupName = groupName.startsWith("GROUP_") ? groupName : "GROUP_" + groupName;
                        return new SimpleGrantedAuthority(groupName);
                    })
                    .collect(Collectors.toSet()));
        }

        System.out.println("Authorities: " + authorities);
        return authorities;
    }
}