package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Collect job definition. Table: bd_collect_job
 */
@Getter
@Setter
@TableName("bd_collect_job")
public class BdCollectJob extends BaseEntity {

    private String jobCode;
    private String jobName;
    private Long sourceId;

    /** CDC / SCHEDULED / MANUAL / STREAMING */
    private String jobType;

    private String cronExpr;
    private String targetTable;
    private String script;
    private Integer enabled;
    private Integer retryTimes;
    private Integer timeoutSeconds;
    private LocalDateTime lastRunTime;

    /** SUCCESS / FAILED / RUNNING */
    private String lastRunStatus;

    private String owner;
    private String remark;
}
