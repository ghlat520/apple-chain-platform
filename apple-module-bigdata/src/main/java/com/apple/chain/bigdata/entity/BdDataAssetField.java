package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Data asset field metadata. Table: bd_data_asset_field
 */
@Getter
@Setter
@TableName("bd_data_asset_field")
public class BdDataAssetField extends BaseEntity {

    private Long assetId;
    private String fieldName;
    private String fieldType;
    private String bizMeaning;
    private Integer isSensitive;

    /** PHONE / ID_CARD / GPS / NAME / EMAIL */
    private String sensitiveType;

    private Integer isPk;
    private Integer nullable;
    private String defaultValue;
    private Integer ordinal;
}
