package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.dto.JobRunCallbackDTO;
import com.apple.chain.bigdata.entity.BdCollectJob;
import com.apple.chain.bigdata.entity.BdCollectJobRun;
import com.apple.chain.bigdata.lineage.DataHubLineageClient;
import com.apple.chain.bigdata.mapper.BdCollectJobRunMapper;
import com.apple.chain.bigdata.service.BdCollectJobService;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DolphinScheduler post-task callback receiver.
 *
 * <p>Each DS Shell node ends with a curl POST to this endpoint after the
 * SeaTunnel job exits. We persist the run record into bd_collect_job_run
 * and (optionally) emit an OpenLineage event for DataHub (M4).</p>
 *
 * <p>See M3-readiness-pack.md §4.4 / §7.</p>
 */
@Slf4j
@Tag(name = "大数据-DS 回调")
@RestController
@RequestMapping("/api/bigdata/job/run")
@RequiredArgsConstructor
public class CollectJobCallbackController {

    private final BdCollectJobService jobService;
    private final BdCollectJobRunMapper runMapper;
    private final DataHubLineageClient lineageClient;

    @Value("${ds.callback.token:changeme}")
    private String expectedToken;

    @Operation(summary = "DS post-task 回调写入 bd_collect_job_run")
    @PostMapping("/callback")
    public R<Long> callback(@RequestHeader(value = "X-DS-Token", required = false) String token,
                            @Valid @RequestBody JobRunCallbackDTO body) {
        if (expectedToken == null || !expectedToken.equals(token)) {
            log.warn("DS callback rejected: bad token, jobCode={}", body.getJobCode());
            return R.fail(1100907, "invalid callback token");
        }

        BdCollectJob job = jobService.getById(body.getJobId());
        if (job == null) {
            return R.fail(1100908, "collect job not found: " + body.getJobId());
        }

        BdCollectJobRun run = new BdCollectJobRun();
        run.setJobId(body.getJobId());
        run.setJobCode(body.getJobCode());
        run.setRunStatus(body.getRunStatus());
        run.setStartTime(body.getStartTime());
        run.setEndTime(body.getEndTime());
        run.setDurationMs(body.getDurationMs());
        run.setRowsRead(body.getRowsRead());
        run.setRowsWritten(body.getRowsWritten());
        run.setErrorMessage(body.getErrorMessage());
        run.setTriggerType(body.getTriggerType() != null ? body.getTriggerType() : "SCHEDULED");
        run.setTriggeredBy(body.getTriggeredBy() != null ? body.getTriggeredBy() : "dolphinscheduler");
        runMapper.insert(run);

        job.setLastRunTime(body.getStartTime());
        job.setLastRunStatus(body.getRunStatus());
        jobService.updateById(job);

        // M3: stub — M4 DataHub 上线后真正发送
        lineageClient.emitJobRun(job, run);

        log.info("DS callback persisted: jobCode={} status={} runId={}",
                body.getJobCode(), body.getRunStatus(), run.getId());
        return R.ok(run.getId());
    }
}
