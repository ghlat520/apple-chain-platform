package com.apple.chain.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT utility using JJWT 0.12.x API.
 * Generates and verifies tokens with userId, username, roleCode claims.
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE_CODE = "roleCode";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMS = "perms";

    @Value("${jwt.secret:apple-chain-platform-jwt-secret-key-must-be-at-least-256-bits}")
    private String secret;

    /**
     * Token TTL in milliseconds. Default 2 hours — short TTL mitigates privilege-persistence
     * risk since RBAC claims are embedded in the JWT with no server-side revocation check.
     * For production, consider adding a Redis token blacklist or switching to refresh-token flow.
     */
    @Value("${jwt.expiration:7200000}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token for a user (legacy single-role variant).
     * Kept for backward compatibility — new code should use the
     * {@link #generateToken(Long, String, String, Collection, Collection)} overload
     * which embeds the user's roles and permission codes.
     */
    public String generateToken(Long userId, String username, String roleCode) {
        return generateToken(userId, username, roleCode,
                roleCode == null ? Collections.emptyList() : Collections.singletonList(roleCode),
                Collections.emptyList());
    }

    /**
     * Generate JWT token carrying full RBAC payload.
     *
     * @param userId      user primary key
     * @param username    login name
     * @param roleCode    primary role code (kept for backward compatibility with legacy clients)
     * @param roles       full set of role codes the user holds
     * @param permissions resolved permission codes (used by RbacInterceptor)
     */
    public String generateToken(Long userId, String username, String roleCode,
                                Collection<String> roles, Collection<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLE_CODE, roleCode);
        claims.put(CLAIM_ROLES, roles == null ? Collections.emptyList() : List.copyOf(roles));
        claims.put(CLAIM_PERMS, permissions == null ? Collections.emptyList() : List.copyOf(permissions));

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parse and validate a JWT token.
     *
     * @return Claims if valid, null if invalid or expired
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT parse failed: {}", e.getMessage());
            return null;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get(CLAIM_USER_ID);
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return userId instanceof Long ? (Long) userId : Long.parseLong(userId.toString());
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get(CLAIM_USERNAME, String.class) : null;
    }

    public String getRoleCode(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get(CLAIM_ROLE_CODE, String.class) : null;
    }

    public boolean isTokenValid(String token) {
        return parseToken(token) != null;
    }
}
