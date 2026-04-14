package com.apple.chain.user.service.impl;

import com.apple.chain.user.service.SmsSenderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Aliyun SMS sender — TODO: integrate Aliyun SMS SDK for production.
 * Active when apple.sms.mock=false.
 */
// @Service
// @ConditionalOnProperty(name = "apple.sms.mock", havingValue = "false")
public class AliyunSmsSenderServiceImpl implements SmsSenderService {

    @Override
    public void sendCode(String phone, String code) {
        throw new UnsupportedOperationException("Aliyun SMS not yet integrated");
    }
}
