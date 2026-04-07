package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Collect job run history. Table: bd_collect_job_run
 */
@Getter
@Setter
@TableName("bd_collect_job_run")
public class BdCollectJobRun extends BaseEntity {

    private Long jobId;
    private String jobCode;

    /** RUNNING / SUCCESS / FAILED / TIMEOUT */
    private String runStatus;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private Long rowsRead;
    private Long rowsWritten;
    private String errorMessage;

    /** SCHEDULED / MANUAL / RETRY */
    private String triggerType;

    private String triggeredBy;
}
