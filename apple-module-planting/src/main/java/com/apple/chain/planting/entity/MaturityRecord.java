package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Maturity sample measurement for a specific orchard.
 *
 * <p>Each record stores the four raw measurements (brix / firmness / color /
 * accumulated temperature), the computed weighted score (0-100) and the
 * categorical recommendation derived from the score thresholds.</p>
 *
 * <p>Table: pt_maturity_record.</p>
 */
@Getter
@Setter
@TableName("pt_maturity_record")
public class MaturityRecord extends BaseEntity {

    /** Orchard the sample was collected from. */
    private Long orchardId;

    /** Variety snapshot at sample time, used to look up the standard. */
    private String variety;

    /** Sample collection date. */
    private LocalDate sampleDate;

    /** Measured sugar Brix degree. */
    private BigDecimal brix;

    /** Measured firmness in kg/cm^2. */
    private BigDecimal firmness;

    /** Measured color in 6-char RGB hex (e.g. C8281E). */
    private String colorRgb;

    /** Accumulated temperature at sample time. */
    private Integer accumulateTemp;

    /** Computed weighted maturity score, 0-100. */
    private BigDecimal maturityScore;

    /** Categorical outcome: UNRIPE / OPTIMAL / OVERRIPE. */
    private String recommendation;

    /** Operator name. */
    private String operator;

    /** Free text remark. */
    private String remark;
}
