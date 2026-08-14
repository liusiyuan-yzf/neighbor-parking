package com.neighborparking.security;

import com.neighborparking.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public class CurrentUser {
    private final Long userId;
    private final Set<UserRole> roles;

    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    public static CurrentUser of(Long userId, Set<UserRole> roles) {
        return new CurrentUser(userId, Collections.unmodifiableSet(new LinkedHashSet<UserRole>(roles)));
    }
}
