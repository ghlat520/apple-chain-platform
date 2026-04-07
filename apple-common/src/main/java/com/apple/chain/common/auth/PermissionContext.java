package com.apple.chain.common.auth;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ThreadLocal carrier for the current request's RBAC payload:
 * the union of role codes and permission codes resolved from the JWT.
 * <p>
 * Populated by {@link RbacInterceptor#preHandle} after token verification, and cleared
 * in {@code afterCompletion} so no thread-pool leakage occurs.
 * <p>
 * Both sets are stored as immutable copies to enforce the project-wide
 * "never mutate shared state" rule.
 */
public final class PermissionContext {

    private static final ThreadLocal<Snapshot> HOLDER = new ThreadLocal<>();

    private PermissionContext() {
    }

    /** Replace the current snapshot with an immutable copy of the given sets. */
    public static void set(Set<String> roles, Set<String> permissions) {
        Set<String> safeRoles = roles == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(roles));
        Set<String> safePerms = permissions == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(permissions));
        HOLDER.set(new Snapshot(safeRoles, safePerms));
    }

    public static Set<String> getRoles() {
        Snapshot snap = HOLDER.get();
        return snap != null ? snap.roles : Collections.emptySet();
    }

    public static Set<String> getPermissions() {
        Snapshot snap = HOLDER.get();
        return snap != null ? snap.permissions : Collections.emptySet();
    }

    public static boolean hasPermission(String code) {
        if (code == null || code.isEmpty()) {
            return true;
        }
        Snapshot snap = HOLDER.get();
        return snap != null && snap.permissions.contains(code);
    }

    public static boolean hasRole(String roleCode) {
        if (roleCode == null || roleCode.isEmpty()) {
            return false;
        }
        Snapshot snap = HOLDER.get();
        return snap != null && snap.roles.contains(roleCode);
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** Immutable per-request snapshot. */
    private static final class Snapshot {
        private final Set<String> roles;
        private final Set<String> permissions;

        private Snapshot(Set<String> roles, Set<String> permissions) {
            this.roles = roles;
            this.permissions = permissions;
        }
    }
}
