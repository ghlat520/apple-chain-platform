package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@TableName("td_quality_inspection")
public class QualityInspection extends BaseEntity {

    private String inspectionNo;
    private Long orderId;
    private Long supplyId;
    private String variety;
    private String grade;

    private BigDecimal brixValue;
    private BigDecimal firmnessValue;
    private BigDecimal colorScore;
    private BigDecimal defectRate;

    private String inspectorName;
    private LocalDate inspectionDate;
    private String reportUrl;

    /** PASS / FAIL / CONDITIONAL */
    private String result;

    /** PENDING / INSPECTED / ACCEPTED / DISPUTED */
    private String status;

    private String remark;
}
