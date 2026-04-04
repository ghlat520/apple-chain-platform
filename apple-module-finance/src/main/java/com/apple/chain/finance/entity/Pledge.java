package com.apple.chain.finance.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@TableName("sf_pledge")
public class Pledge extends BaseEntity {

    private String pledgeCode;
    private Long receiptId;
    private String receiptCode;
    private String pledgorName;
    private String pledgeeName;
    private String commodity;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal appraisedValue;
    private BigDecimal pledgeRate;
    private BigDecimal loanAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    /** PENDING/ACTIVE/RELEASED/DEFAULTED */
    private String status;
    private String remark;
}
