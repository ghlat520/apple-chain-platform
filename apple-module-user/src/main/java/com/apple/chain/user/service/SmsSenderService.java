package com.apple.chain.user.service;

/**
 * SMS sender service — sends verification codes to phones.
 * Implementations: Mock (log-only) for dev, Aliyun for production.
 */
public interface SmsSenderService {

    /**
     * Send a verification code to the given phone number.
     *
     * @param phone target phone number
     * @param code  6-digit verification code
     */
    void sendCode(String phone, String code);
}
