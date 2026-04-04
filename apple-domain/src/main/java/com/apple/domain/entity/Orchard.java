package com.apple.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Orchard (果园) — farmer-owned land for apple cultivation.
 */
@Data
@TableName("orchard")
public class Orchard {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Auto-generated code like OC202404010001 */
    private String orchardCode;

    private Long farmerUserId;

    private String farmerName;

    private String orchardName;

    private String province;

    private String city;

    private String district;

    private String address;

    /** Area in 亩 */
    private BigDecimal area;

    /** 品种: 红富士/嘎啦/华冠/秦冠/乔纳金 etc. */
    private String variety;

    /** 无/绿色/有机 */
    private String certificationLevel;

    /** active / inactive */
    private String status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
