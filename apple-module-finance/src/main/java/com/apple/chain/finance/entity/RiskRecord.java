package com.apple.chain.finance.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("sf_risk_record")
public class RiskRecord extends BaseEntity {

    private String riskCode;
    private String relatedType;
    private Long relatedId;
    /** HIGH/MEDIUM/LOW */
    private String riskLevel;
    /** OVERDUE/PRICE_DROP/QUALITY/FRAUD */
    private String riskType;
    private String description;
    private String measure;
    private String handler;
    private LocalDateTime handleTime;
    /** OPEN/HANDLING/RESOLVED/CLOSED */
    private String status;
    private String remark;
}
