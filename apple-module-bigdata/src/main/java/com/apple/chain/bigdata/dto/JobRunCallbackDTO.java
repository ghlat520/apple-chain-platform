package com.apple.chain.bigdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DolphinScheduler post-task callback payload.
 *
 * <p>Posted by DS Shell node at the end of every SeaTunnel workflow.
 * See M3-readiness-pack.md §4.4 / §7.</p>
 */
@Getter
@Setter
public class JobRunCallbackDTO {

    /** Maps to bd_collect_job.id */
    @NotNull
    private Long jobId;

    @NotBlank
    private String jobCode;

    /** SUCCESS / FAILED / TIMEOUT */
    @NotBlank
    private String runStatus;

    @NotNull
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private Long rowsRead;

    private Long rowsWritten;

    private String errorMessage;

    /** SCHEDULED / MANUAL / RETRY */
    private String triggerType;

    private String triggeredBy;

    /** DS workflow instance id — for cross-system tracing */
    private String dsWorkflowInstanceId;
}
