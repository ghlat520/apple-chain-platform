package com.apple.chain.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method-level RBAC permission gate.
 * <p>
 * Usage: {@code @RequirePerm("orchard:write")} on a controller handler.
 * The {@link RbacInterceptor} (registered for {@code /api/**}) reads this annotation
 * after JWT validation and rejects the request with HTTP 403 when the current user
 * does not own the required permission code.
 * <p>
 * Multiple codes can be specified — semantics is configurable via {@link #logical()}:
 * <ul>
 *   <li>{@link Logical#AND} — user must own ALL listed permissions (default)</li>
 *   <li>{@link Logical#OR}  — user must own AT LEAST ONE of the listed permissions</li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePerm {

    /** Required permission code(s), e.g. {@code "orchard:write"}. */
    String[] value();

    /** Combination logic. Default AND. */
    Logical logical() default Logical.AND;

    enum Logical { AND, OR }
}
