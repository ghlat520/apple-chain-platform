package com.apple.chain.finance.gateway;

/**
 * M8 electronic-contract gateway. Wraps e签宝 (or 法大大) SDK.
 * Default impl is mock; switch via {@code finance.gateway.contract.real=true}.
 */
public interface ContractGateway {

    /**
     * Create a contract draft from a template.
     *
     * @param orderId    business order id
     * @param partyAUid  user id of side A
     * @param partyBUid  user id of side B
     * @return external contract number (assigned by the provider)
     */
    String createContract(Long orderId, Long partyAUid, Long partyBUid);

    /**
     * Sign the contract on behalf of a user.
     *
     * @return signed PDF URL (mock impl writes to MinIO {@code mock-contracts/} bucket path)
     */
    String signContract(String contractNo, Long signerUid);
}
