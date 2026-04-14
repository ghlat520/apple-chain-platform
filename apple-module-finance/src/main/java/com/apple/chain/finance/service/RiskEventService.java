package com.apple.chain.finance.service;

import com.apple.chain.finance.dto.RiskEventHandleRequest;
import com.apple.chain.finance.entity.RiskEvent;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Risk event query + handling.
 */
public interface RiskEventService extends IService<RiskEvent> {

    IPage<RiskEvent> listEvents(int page, int size, String severity, String status, String targetType);

    RiskEvent getEventDetail(Long id);

    /**
     * Move an event through its lifecycle. Allowed transitions:
     *   PENDING   → HANDLING / RESOLVED / IGNORED
     *   HANDLING  → RESOLVED / IGNORED
     *   RESOLVED  → (terminal)
     *   IGNORED   → (terminal)
     */
    RiskEvent handleEvent(Long id, RiskEventHandleRequest request);
}
