package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdCollectJob;
import com.apple.chain.bigdata.entity.BdCollectJobRun;
import com.apple.chain.bigdata.mapper.BdCollectJobMapper;
import com.apple.chain.bigdata.mapper.BdCollectJobRunMapper;
import com.apple.chain.bigdata.service.BdCollectJobService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BdCollectJobServiceImpl
        extends ServiceImpl<BdCollectJobMapper, BdCollectJob>
        implements BdCollectJobService {

    private final BdCollectJobRunMapper runMapper;

    @Override
    public BdCollectJobRun triggerJob(Long jobId, String triggeredBy) {
        BdCollectJob job = getById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("collect job not found: " + jobId);
        }
        BdCollectJobRun run = new BdCollectJobRun();
        run.setJobId(jobId);
        run.setJobCode(job.getJobCode());
        run.setRunStatus("RUNNING");
        run.setStartTime(LocalDateTime.now());
        run.setTriggerType("MANUAL");
        run.setTriggeredBy(triggeredBy);
        runMapper.insert(run);

        job.setLastRunTime(run.getStartTime());
        job.setLastRunStatus("RUNNING");
        updateById(job);
        return run;
    }

    @Override
    public IPage<BdCollectJobRun> listRuns(Long jobId, int page, int size) {
        LambdaQueryWrapper<BdCollectJobRun> wrapper = new LambdaQueryWrapper<>();
        if (jobId != null) {
            wrapper.eq(BdCollectJobRun::getJobId, jobId);
        }
        wrapper.orderByDesc(BdCollectJobRun::getStartTime);
        return runMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
