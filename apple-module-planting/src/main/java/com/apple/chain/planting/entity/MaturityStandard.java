package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Maturity standard baseline per apple variety.
 *
 * <p>Source: design-patches-12-missing.md M6 §6.3.</p>
 *
 * <p>Acts as the immutable reference point for the 4-factor weighted maturity
 * scoring algorithm: brix (0.4) + firmness (0.3) + color (0.2) + accumulated
 * temperature (0.1).</p>
 *
 * <p>Table: pt_maturity_standard.</p>
 */
@Getter
@Setter
@TableName("pt_maturity_standard")
public class MaturityStandard extends BaseEntity {

    /** Variety code, e.g. red_fuji / gala / golden_delicious. */
    private String variety;

    /** Sugar Brix lower bound (degrees). */
    private BigDecimal brixMin;

    /** Sugar Brix upper bound (degrees). */
    private BigDecimal brixMax;

    /** Firmness lower bound kg/cm^2. */
    private BigDecimal firmnessMin;

    /** Firmness upper bound kg/cm^2. */
    private BigDecimal firmnessMax;

    /** Target color in 6-char RGB hex (e.g. C8281E for deep red). */
    private String colorTarget;

    /** Accumulated temperature target in degree-days. */
    private Integer accumulateTempTarget;

    /**
     * Average daily accumulated temperature increment used to predict
     * "ready in N days" for under-ripe samples.
     */
    private BigDecimal dailyTempIncrement;

    /** Free text description. */
    private String description;
}
