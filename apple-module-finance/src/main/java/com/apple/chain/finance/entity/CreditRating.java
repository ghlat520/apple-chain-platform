package com.apple.chain.finance.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@TableName("sf_credit_rating")
public class CreditRating extends BaseEntity {

    private String entityType;
    private Long entityId;
    private String entityName;
    private Integer creditScore;
    private String creditLevel;
    private LocalDate assessmentDate;
    private LocalDate validUntil;
    private Integer tradeScore;
    private Integer productionScore;
    private Integer financialScore;
    private String assessor;
    private String remark;
    /** ACTIVE/EXPIRED/REVOKED */
    private String status;
}
