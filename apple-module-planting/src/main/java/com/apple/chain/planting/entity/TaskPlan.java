package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * M5 作业计划. Table: pt_task_plan (V18).
 */
@Getter
@Setter
@TableName("pt_task_plan")
public class TaskPlan extends BaseEntity {

    private Long orchardId;
    private Long templateId;
    private String operationType;
    private String taskName;
    private LocalDate planDate;
    private Integer priority;
    private String materialSuggestion;
    private String status;
    private Long actualOperationId;
    private String generatedBy;
    private String remark;

    public static final String STATUS_PENDING     = "PENDING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_DONE        = "DONE";
    public static final String STATUS_SKIPPED     = "SKIPPED";

    public static final String GEN_AUTO   = "AUTO";
    public static final String GEN_MANUAL = "MANUAL";
}
