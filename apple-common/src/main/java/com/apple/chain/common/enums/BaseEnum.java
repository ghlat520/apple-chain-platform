package com.apple.chain.common.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Contract for business enums that cross the wire to the front-end.
 *
 * <p>All business enums (e.g. {@code UserStatus}, {@code OrderStatus}) MUST
 * implement this interface. Jackson will serialize them as an object
 * {@code {"code": N, "desc": "..."}} rather than a raw integer or enum name,
 * so the front-end always has both the machine value and the display label in
 * a single field.
 *
 * <p>To enable the object-shape serialization, annotate the implementing enum
 * with {@code @JsonFormat(shape = OBJECT)}. Example:
 *
 * <pre>{@code
 * @Getter
 * @AllArgsConstructor
 * @JsonFormat(shape = JsonFormat.Shape.OBJECT)
 * public enum UserStatus implements BaseEnum {
 *     ACTIVE(1, "正常"),
 *     LOCKED(2, "锁定");
 *
 *     private final int code;
 *     private final String desc;
 * }
 * }</pre>
 *
 * <p>See {@code docs/contract/BACKEND-CONTRACT.md} section 5 for the full rule.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public interface BaseEnum {

    /**
     * Numeric code persisted in the database and used by the front-end in
     * filter queries. Stable once released.
     */
    int getCode();

    /**
     * Human-readable Chinese description shown directly in the UI.
     */
    String getDesc();
}
