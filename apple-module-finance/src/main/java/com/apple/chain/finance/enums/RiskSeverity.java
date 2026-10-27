package com.apple.chain.finance.enums;

import com.apple.chain.common.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * Risk severity tier for warning rules / events.
 *
 * <p>Persisted in DB as the enum name (e.g. {@code "HIGH"}), serialized to the
 * front-end as {@code {"code": 3, "desc": "高"}} per BaseEnum contract.
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RiskSeverity implements BaseEnum {

    LOW(1, "低"),
    MEDIUM(2, "中"),
    HIGH(3, "高"),
    CRITICAL(4, "严重");

    @EnumValue
    private final int code;
    private final String desc;

    RiskSeverity(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static RiskSeverity fromAny(Object value) {
        if (value == null) return null;
        if (value instanceof RiskSeverity rs) return rs;
        String s = String.valueOf(value).trim();
        for (RiskSeverity v : values()) {
            if (v.name().equalsIgnoreCase(s) || String.valueOf(v.code).equals(s) || v.desc.equals(s)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown RiskSeverity: " + value);
    }
}
