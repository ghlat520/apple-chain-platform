package com.apple.chain.finance.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.finance.dto.RiskEventHandleRequest;
import com.apple.chain.finance.entity.RiskEvent;
import com.apple.chain.finance.enums.RiskEventStatus;
import com.apple.chain.finance.enums.RiskSeverity;
import com.apple.chain.finance.mapper.RiskEventMapper;
import com.apple.chain.finance.service.RiskEventService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Service
public class RiskEventServiceImpl extends ServiceImpl<RiskEventMapper, RiskEvent> implements RiskEventService {

    /** Terminal statuses cannot transition further. */
    private static final Set<RiskEventStatus> TERMINAL =
            EnumSet.of(RiskEventStatus.RESOLVED, RiskEventStatus.IGNORED);

    @Override
    public IPage<RiskEvent> listEvents(int page, int size, String severity, String status, String targetType) {
        RiskSeverity sev = parseEnum(severity, RiskSeverity::fromAny, "severity");
        RiskEventStatus st = parseEnum(status, RiskEventStatus::fromAny, "status");
        LambdaQueryWrapper<RiskEvent> wrapper = new LambdaQueryWrapper<RiskEvent>()
                .eq(sev != null, RiskEvent::getSeverity, sev)
                .eq(st != null, RiskEvent::getStatus, st)
                .eq(StringUtils.hasText(targetType), RiskEvent::getTargetType, targetType)
                .orderByDesc(RiskEvent::getTriggerTime)
                .orderByDesc(RiskEvent::getId);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public RiskEvent getEventDetail(Long id) {
        RiskEvent event = getById(id);
        if (event == null) throw new BizException(ResultCode.NOT_FOUND, "风控事件不存在");
        return event;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskEvent handleEvent(Long id, RiskEventHandleRequest request) {
        if (request == null || request.getStatus() == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "目标状态不能为空");
        }
        RiskEvent event = getEventDetail(id);
        RiskEventStatus next = request.getStatus();

        if (event.getStatus() != null && TERMINAL.contains(event.getStatus())) {
            throw new BizException("事件已结案，不能再次处置");
        }
        if (next == RiskEventStatus.PENDING) {
            throw new BizException(ResultCode.PARAM_ERROR, "不能将事件回退到待处理状态");
        }

        event.setStatus(next);
        if (request.getAssigneeId() != null) {
            event.setAssigneeId(request.getAssigneeId());
        }
        if (request.getHandleRemark() != null) {
            event.setHandleRemark(request.getHandleRemark());
        }
        if (TERMINAL.contains(next)) {
            event.setHandleTime(LocalDateTime.now());
        } else if (next == RiskEventStatus.HANDLING && event.getHandleTime() == null) {
            // first time someone picks it up; leave handleTime null until it closes
        }
        updateById(event);
        return getById(id);
    }

    // ── helpers ─────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface EnumParser<T> { T parse(Object value); }

    private static <T> T parseEnum(String raw, EnumParser<T> parser, String fieldName) {
        if (!StringUtils.hasText(raw)) return null;
        try {
            return parser.parse(raw);
        } catch (IllegalArgumentException ex) {
            throw new BizException(ResultCode.PARAM_ERROR, "未知的 " + fieldName + ": " + raw);
        }
    }
}
