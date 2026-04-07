package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdCollectJob;
import com.apple.chain.bigdata.entity.BdCollectJobRun;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/** Collect job definition + run history. */
public interface BdCollectJobService extends IService<BdCollectJob> {

    /** Manually trigger a job; records a RUNNING row. */
    BdCollectJobRun triggerJob(Long jobId, String triggeredBy);

    /** Run history page for a given job. */
    IPage<BdCollectJobRun> listRuns(Long jobId, int page, int size);
}
