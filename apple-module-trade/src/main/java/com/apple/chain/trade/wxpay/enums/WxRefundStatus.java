package com.apple.chain.trade.wxpay.enums;

import com.apple.chain.common.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * 微信支付 Mock — 退款状态枚举。
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum WxRefundStatus implements BaseEnum {

    PENDING(1, "退款处理中"),
    SUCCESS(2, "退款成功"),
    FAILED(3, "退款失败");

    @EnumValue
    private final int code;
    private final String desc;

    WxRefundStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static WxRefundStatus fromAny(Object value) {
        if (value == null) return null;
        if (value instanceof WxRefundStatus v) return v;
        String s = String.valueOf(value).trim();
        for (WxRefundStatus v : values()) {
            if (v.name().equalsIgnoreCase(s) || String.valueOf(v.code).equals(s) || v.desc.equals(s)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown WxRefundStatus: " + value);
    }
}
