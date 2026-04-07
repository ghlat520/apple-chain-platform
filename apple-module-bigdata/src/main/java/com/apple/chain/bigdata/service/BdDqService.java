package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdDqCheckResult;
import com.apple.chain.bigdata.entity.BdDqRule;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/** Data quality rules + check results. */
public interface BdDqService extends IService<BdDqRule> {

    /** Manually run a rule; records a check result row (stub in M2, real in M4). */
    BdDqCheckResult runCheck(Long ruleId);

    IPage<BdDqCheckResult> listResults(Long ruleId, int page, int size);
}
