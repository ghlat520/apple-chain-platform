package com.apple.chain.finance.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@TableName("sf_loan")
public class Loan extends BaseEntity {

    private String loanCode;
    private String borrowerName;
    private String borrowerType;
    private Long borrowerId;
    /** PLEDGE/RECEIVABLE/CREDIT */
    private String loanType;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private LocalDate applyDate;
    private LocalDate approveDate;
    private LocalDate disburseDate;
    private LocalDate dueDate;
    private BigDecimal repaidAmount;
    /** PENDING/APPROVED/REJECTED/DISBURSED/REPAID/OVERDUE */
    private String status;
    private Long pledgeId;
    private String remark;
}
