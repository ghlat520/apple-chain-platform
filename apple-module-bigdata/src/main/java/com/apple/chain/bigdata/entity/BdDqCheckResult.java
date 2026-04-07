package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data quality check result. Table: bd_dq_check_result
 */
@Getter
@Setter
@TableName("bd_dq_check_result")
public class BdDqCheckResult extends BaseEntity {

    private Long ruleId;
    private String ruleCode;
    private LocalDateTime checkTime;
    private Long totalRows;
    private Long badRows;
    private BigDecimal score;
    private Integer pass;
    private String sampleBadRows;
    private String errorMessage;
}
