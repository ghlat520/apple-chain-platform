package com.apple.chain.user.service.impl;

import com.apple.chain.user.service.SmsSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Mock SMS sender — logs code to console. Active when apple.sms.mock=true.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "apple.sms.mock", havingValue = "true", matchIfMissing = true)
public class MockSmsSenderServiceImpl implements SmsSenderService {

    @Override
    public void sendCode(String phone, String code) {
        log.info("SMS mock: code={} sent to {}", code, phone);
    }
}
