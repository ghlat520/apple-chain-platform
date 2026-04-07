package com.apple.chain.finance.gateway.impl;

import com.apple.chain.finance.gateway.ContractGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock e签宝 contract gateway. Returns deterministic-looking contract numbers
 * and synthetic PDF URLs without touching MinIO (test-friendly).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "finance.gateway.contract.real",
        havingValue = "false", matchIfMissing = true)
public class MockESignContractGateway implements ContractGateway {

    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public String createContract(Long orderId, Long partyAUid, Long partyBUid) {
        long seq = counter.getAndIncrement();
        String no = String.format("MOCK-CONTRACT-%d-%06d", orderId == null ? 0 : orderId, seq);
        log.debug("Mock contract created: {} A={} B={}", no, partyAUid, partyBUid);
        return no;
    }

    @Override
    public String signContract(String contractNo, Long signerUid) {
        return "mock://contracts/" + contractNo + "/" + signerUid + ".pdf";
    }
}
