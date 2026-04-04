package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Farmer (农户) entity.
 * Table: farm_farmer
 */
@Getter
@Setter
@TableName("farm_farmer")
public class Farmer extends BaseEntity {

    /** Auto-generated farmer code: FC + yyyyMMdd + 4-digit seq */
    private String farmerCode;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String idCard;

    private String address;

    /** Registered planting area in mu */
    private BigDecimal registeredArea;

    /** ACTIVE / INACTIVE */
    private String status;
}
