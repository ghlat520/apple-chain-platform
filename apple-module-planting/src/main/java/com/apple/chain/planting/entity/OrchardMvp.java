package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * MVP Orchard entity.
 * Table: farm_orchard
 *
 * Status: ACTIVE / INACTIVE
 */
@Getter
@Setter
@TableName("farm_orchard")
public class OrchardMvp extends BaseEntity {

    /** Auto-generated code: OC + yyyyMMdd + 4-digit seq */
    private String orchardCode;

    @NotBlank(message = "果园名称不能为空")
    private String orchardName;

    @NotNull(message = "农户ID不能为空")
    private Long farmerId;

    private String location;

    /** Area in mu */
    private BigDecimal area;

    private String variety;

    private Integer plantingYear;

    /** ACTIVE / INACTIVE */
    private String status;
}
