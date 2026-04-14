package com.apple.chain.finance.enums;

import com.apple.chain.common.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * Lifecycle status of a {@code RiskEvent}:
 * PENDING → HANDLING → RESOLVED / IGNORED.
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RiskEventStatus implements BaseEnum {

    PENDING(1, "待处理"),
    HANDLING(2, "处理中"),
    RESOLVED(3, "已解决"),
    IGNORED(4, "已忽略");

    @EnumValue
    private final int code;
    private final String desc;

    RiskEventStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static RiskEventStatus fromAny(Object value) {
        if (value == null) return null;
        if (value instanceof RiskEventStatus rs) return rs;
        String s = String.valueOf(value).trim();
        for (RiskEventStatus v : values()) {
            if (v.name().equalsIgnoreCase(s) || String.valueOf(v.code).equals(s) || v.desc.equals(s)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown RiskEventStatus: " + value);
    }
}
