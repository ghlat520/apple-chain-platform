package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data asset catalog (table level). Table: bd_data_asset
 */
@Getter
@Setter
@TableName("bd_data_asset")
public class BdDataAsset extends BaseEntity {

    private String assetCode;
    private String databaseName;
    private String schemaName;
    private String tableName;
    private String bizName;
    private String bizDescription;
    private String owner;

    /** PUBLIC / INTERNAL / SENSITIVE / SECRET */
    private String securityLevel;

    private String sourceSystem;
    private Long recordCount;
    private LocalDateTime lastUpdateTime;
    private String tags;
}
