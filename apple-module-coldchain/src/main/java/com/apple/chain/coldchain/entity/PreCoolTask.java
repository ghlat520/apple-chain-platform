package com.apple.chain.coldchain.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("cc_pre_cool_task")
public class PreCoolTask extends BaseEntity {

    private String taskNo;
    private Long vehicleId;
    private String batchCode;
    private BigDecimal startTemp;
    /** default 2.0 */
    private BigDecimal targetTemp;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** duration in minutes */
    private Integer duration;
    /** PENDING/COOLING/COMPLETED/FAILED */
    private String status;
    private String operator;
    private String remark;
}
