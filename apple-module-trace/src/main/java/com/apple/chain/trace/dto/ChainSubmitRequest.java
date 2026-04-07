package com.apple.chain.trace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * M2: request body for the manual chain submit endpoint.
 * Used by internal service-to-service calls that don't go through the
 * automatic M12 hook (e.g. cultivation/trade snapshots).
 */
@Data
public class ChainSubmitRequest {

    @NotBlank(message = "traceCode 不能为空")
    @Size(max = 64)
    private String traceCode;

    @NotBlank(message = "businessType 不能为空")
    @Size(max = 32)
    private String businessType;

    private Long businessId;

    /** Snapshot will be canonicalized + SHA-256 hashed server side. */
    private Map<String, Object> snapshot;
}
