package com.apple.chain.finance.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.finance.dto.RiskRuleRequest;
import com.apple.chain.finance.entity.RiskRule;
import com.apple.chain.finance.enums.RiskSeverity;
import com.apple.chain.finance.mapper.RiskRuleMapper;
import com.apple.chain.finance.service.RiskRuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RiskRuleServiceImpl extends ServiceImpl<RiskRuleMapper, RiskRule> implements RiskRuleService {

    @Override
    public IPage<RiskRule> listRules(int page, int size, String keyword, String severity, Boolean enabled) {
        RiskSeverity sev = parseSeverity(severity);
        LambdaQueryWrapper<RiskRule> wrapper = new LambdaQueryWrapper<RiskRule>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(RiskRule::getName, keyword)
                        .or().like(RiskRule::getRuleType, keyword))
                .eq(sev != null, RiskRule::getSeverity, sev)
                .eq(enabled != null, RiskRule::getEnabled, enabled)
                .orderByDesc(RiskRule::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public RiskRule getRuleDetail(Long id) {
        RiskRule rule = getById(id);
        if (rule == null) throw new BizException(ResultCode.NOT_FOUND, "风控规则不存在");
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskRule createRule(RiskRuleRequest request, Long creatorId) {
        validateRequest(request);
        RiskRule rule = new RiskRule();
        rule.setName(request.getName().trim());
        rule.setRuleType(request.getRuleType().trim());
        rule.setThreshold(request.getThreshold());
        rule.setSeverity(request.getSeverity());
        rule.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        rule.setCreatorId(creatorId);
        save(rule);
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskRule updateRule(Long id, RiskRuleRequest request) {
        validateRequest(request);
        RiskRule existing = getRuleDetail(id);
        existing.setName(request.getName().trim());
        existing.setRuleType(request.getRuleType().trim());
        existing.setThreshold(request.getThreshold());
        existing.setSeverity(request.getSeverity());
        if (request.getEnabled() != null) {
            existing.setEnabled(request.getEnabled());
        }
        updateById(existing);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        getRuleDetail(id);
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskRule setEnabled(Long id, boolean enabled) {
        RiskRule existing = getRuleDetail(id);
        if (existing.getEnabled() != null && existing.getEnabled() == enabled) {
            return existing;
        }
        existing.setEnabled(enabled);
        updateById(existing);
        return getById(id);
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private void validateRequest(RiskRuleRequest request) {
        if (request == null) throw new BizException(ResultCode.PARAM_ERROR, "请求体不能为空");
        if (!StringUtils.hasText(request.getName())) throw new BizException(ResultCode.PARAM_ERROR, "规则名称不能为空");
        if (!StringUtils.hasText(request.getRuleType())) throw new BizException(ResultCode.PARAM_ERROR, "规则类型不能为空");
        if (request.getThreshold() == null) throw new BizException(ResultCode.PARAM_ERROR, "阈值不能为空");
        if (request.getSeverity() == null) throw new BizException(ResultCode.PARAM_ERROR, "严重度不能为空");
    }

    private RiskSeverity parseSeverity(String s) {
        if (!StringUtils.hasText(s)) return null;
        try {
            return RiskSeverity.fromAny(s);
        } catch (IllegalArgumentException ex) {
            throw new BizException(ResultCode.PARAM_ERROR, "未知的严重度: " + s);
        }
    }
}
