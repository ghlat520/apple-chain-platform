package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Open API call log. Table: bd_api_call_log
 */
@Getter
@Setter
@TableName("bd_api_call_log")
public class BdApiCallLog extends BaseEntity {

    private String appKey;
    private String clientIp;
    private String apiPath;
    private String method;
    private Integer statusCode;
    private Long durationMs;
    private Long requestBytes;
    private Long responseBytes;
    private String errorMessage;
}
