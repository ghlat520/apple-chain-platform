package com.apple.chain.iot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Edge gateway → ingest endpoint payload. One row per measurement.
 * Validation kept light; the bulk endpoint accepts large arrays so we
 * don't @Valid each row individually (that's a perf cliff for batches of 1000+).
 */
@Data
public class TelemetryDTO {

    @NotBlank private String deviceSn;
    @NotNull  private LocalDateTime collectTime;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitude;
    private BigDecimal speed;
    private BigDecimal temp1;
    private BigDecimal temp2;
    private BigDecimal temp3;
    private BigDecimal temp4;
    private BigDecimal humidity;
    private Integer doorStatus;
    private String signalType;
}
