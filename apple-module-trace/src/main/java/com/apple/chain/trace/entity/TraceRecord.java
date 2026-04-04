package com.apple.chain.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Trace record entity — one event/stage in the traceability timeline.
 * Table: trace_record
 *
 * Stage values: HARVEST / STORAGE / TRANSPORT / SALE
 * Note: No soft-delete for records (audit trail must be preserved).
 */
@Getter
@Setter
@TableName("trace_record")
public class TraceRecord implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private String batchCode;

    /** HARVEST / STORAGE / TRANSPORT / SALE */
    private String stage;

    private String operator;

    private String operatorPhone;

    private String location;

    /** Temperature in Celsius, optional */
    private Double temperature;

    /** Humidity percentage, optional */
    private Double humidity;

    private String remark;

    private LocalDateTime recordTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
}
