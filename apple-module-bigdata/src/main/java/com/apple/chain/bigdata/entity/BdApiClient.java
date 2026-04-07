package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Open API client. Table: bd_api_client
 */
@Getter
@Setter
@TableName("bd_api_client")
public class BdApiClient extends BaseEntity {

    private String clientName;
    private String appKey;

    /** AES-encrypted secret. Never return in plain. */
    private String appSecretEnc;

    private String owner;
    private Integer rateLimitQps;
    private Integer dailyQuota;

    /** ACTIVE / DISABLED / REVOKED */
    private String status;

    private LocalDate validUntil;
    private String remark;
}
