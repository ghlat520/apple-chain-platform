package com.apple.chain.finance.service;

import com.apple.chain.finance.dto.RiskRuleRequest;
import com.apple.chain.finance.entity.RiskRule;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * CRUD + lifecycle for risk warning rules.
 */
public interface RiskRuleService extends IService<RiskRule> {

    IPage<RiskRule> listRules(int page, int size, String keyword, String severity, Boolean enabled);

    RiskRule getRuleDetail(Long id);

    RiskRule createRule(RiskRuleRequest request, Long creatorId);

    RiskRule updateRule(Long id, RiskRuleRequest request);

    void deleteRule(Long id);

    /** Toggle a rule's enabled flag. Returns the updated rule. */
    RiskRule setEnabled(Long id, boolean enabled);
}
