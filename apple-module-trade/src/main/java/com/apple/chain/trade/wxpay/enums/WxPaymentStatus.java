package com.apple.chain.trade.wxpay.enums;

import com.apple.chain.common.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * 微信支付 Mock — 支付状态枚举。
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum WxPaymentStatus implements BaseEnum {

    PENDING(1, "待支付"),
    SUCCESS(2, "支付成功"),
    FAILED(3, "支付失败"),
    REFUNDED(4, "已退款");

    @EnumValue
    private final int code;
    private final String desc;

    WxPaymentStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static WxPaymentStatus fromAny(Object value) {
        if (value == null) return null;
        if (value instanceof WxPaymentStatus v) return v;
        String s = String.valueOf(value).trim();
        for (WxPaymentStatus v : values()) {
            if (v.name().equalsIgnoreCase(s) || String.valueOf(v.code).equals(s) || v.desc.equals(s)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown WxPaymentStatus: " + value);
    }

    public static WxPaymentStatus fromName(String name) {
        if (name == null || name.isBlank()) return null;
        for (WxPaymentStatus v : values()) {
            if (v.name().equalsIgnoreCase(name.trim())) return v;
        }
        return null;
    }
}
