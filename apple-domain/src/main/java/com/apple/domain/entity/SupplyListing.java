package com.apple.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Supply listing (供应发布) — farmer publishes available apple batch for sale.
 */
@Data
@TableName("supply_listing")
public class SupplyListing {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Auto-generated code like SL202404010001 */
    private String listingCode;

    private Long batchId;

    private Long sellerUserId;

    private String sellerName;

    private String variety;

    /** 一级/二级/三级/等外 */
    private String grade;

    /** Available weight in kg */
    private BigDecimal weight;

    private BigDecimal pricePerKg;

    private String province;

    private String city;

    private String description;

    /** open / negotiating / sold / expired */
    private String status;

    private LocalDateTime expiredAt;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
