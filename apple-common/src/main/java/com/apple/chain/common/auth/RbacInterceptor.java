package com.apple.chain.common.auth;

import com.apple.chain.common.context.UserContext;
import com.apple.chain.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RBAC interceptor: runs <em>after</em> {@link com.apple.chain.common.interceptor.AuthInterceptor}
 * (lower order so it executes second). Resolves the current user's role and permission
 * codes from the JWT claims, populates {@link PermissionContext}, and enforces
 * {@link RequirePerm @RequirePerm} on the matched handler method (or its declaring class).
 * <p>
 * Why JWT-claim based (not DB lookup per request)?
 * <ul>
 *   <li>Zero DB hit on the hot path. The token already carries the user identity;
 *       the issuer (login flow) embeds the resolved {@code roles} and {@code perms}.</li>
 *   <li>Revocation strategy: short token TTL + relogin. Acceptable for M1 scope.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RbacInterceptor implements HandlerInterceptor {

    static final String CLAIM_ROLES = "roles";
    static final String CLAIM_PERMS = "perms";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Populate PermissionContext for every authenticated request, even when no
        // @RequirePerm annotation exists, so service-layer code can call hasPermission().
        Claims claims = parseClaims(request);
        if (claims != null) {
            Set<String> roles = readStringSet(claims, CLAIM_ROLES);
            // Backward-compat: legacy tokens carry only roleCode (single string).
            if (roles.isEmpty()) {
                String legacyRole = UserContext.getRoleCode();
                if (StringUtils.hasText(legacyRole)) {
                    roles = Collections.singleton(legacyRole);
                }
            }
            Set<String> perms = readStringSet(claims, CLAIM_PERMS);
            PermissionContext.set(roles, perms);
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequirePerm annotation = handlerMethod.getMethodAnnotation(RequirePerm.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequirePerm.class);
        }
        if (annotation == null) {
            return true;
        }

        String[] required = annotation.value();
        if (required == null || required.length == 0) {
            return true;
        }

        boolean granted = annotation.logical() == RequirePerm.Logical.AND
                ? Arrays.stream(required).allMatch(PermissionContext::hasPermission)
                : Arrays.stream(required).anyMatch(PermissionContext::hasPermission);

        if (!granted) {
            log.warn("RBAC deny user={} uri={} required={}",
                    UserContext.getUsername(), request.getRequestURI(), Arrays.toString(required));
            throw new ForbiddenException("权限不足: 需要 " + String.join(",", required));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        PermissionContext.clear();
    }

    private Claims parseClaims(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return jwtUtil.parseToken(authHeader.substring(BEARER_PREFIX.length()));
    }

    @SuppressWarnings("unchecked")
    private Set<String> readStringSet(Claims claims, String key) {
        Object raw = claims.get(key);
        if (raw instanceof List<?> list) {
            Set<String> result = new HashSet<>(list.size());
            for (Object item : list) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        return Collections.emptySet();
    }
}
