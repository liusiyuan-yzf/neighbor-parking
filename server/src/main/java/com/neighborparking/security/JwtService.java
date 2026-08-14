package com.neighborparking.security;

import com.neighborparking.domain.AppUser;
import com.neighborparking.domain.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(AppUser user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toList());
        return Jwts.builder()
                .setIssuer(properties.getIssuer())
                .setSubject(String.valueOf(user.getId()))
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(properties.getAccessTokenMinutes(), ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public CurrentUser parseToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).requireIssuer(properties.getIssuer())
                .build().parseClaimsJws(token).getBody();
        List<String> roleNames = claims.get("roles", List.class);
        Set<UserRole> roles = roleNames.stream().map(UserRole::valueOf).collect(Collectors.toSet());
        return CurrentUser.of(Long.valueOf(claims.getSubject()), roles);
    }
}
