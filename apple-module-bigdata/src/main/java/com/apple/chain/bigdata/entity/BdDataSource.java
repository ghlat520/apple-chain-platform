package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data source registry. Table: bd_data_source
 * Covers 8 internal modules + 4 external APIs (weather/market/policy/consumer).
 */
@Getter
@Setter
@TableName("bd_data_source")
public class BdDataSource extends BaseEntity {

    private String sourceCode;
    private String sourceName;

    /** MYSQL / CLICKHOUSE / KAFKA / HTTP_API / RSS / FILE */
    private String sourceType;

    /** INTERNAL / EXTERNAL */
    private String category;

    private String connectUrl;
    private String username;

    /** AES-encrypted password. Never return to client. */
    private String passwordEnc;

    private String extraConfig;
    private String owner;

    /** ACTIVE / DISABLED / ERROR */
    private String status;

    private LocalDateTime lastTestTime;
    private String lastTestResult;
    private String remark;
}
