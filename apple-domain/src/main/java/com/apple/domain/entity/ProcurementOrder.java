package com.apple.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Procurement order (收购订单) — buyer places order against a supply listing.
 */
@Data
@TableName("procurement_order")
public class ProcurementOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Auto-generated code like PO202404010001 */
    private String orderCode;

    private Long listingId;

    private Long buyerUserId;

    private String buyerName;

    private Long sellerUserId;

    private String sellerName;

    private String variety;

    private String grade;

    private BigDecimal weightKg;

    private BigDecimal pricePerKg;

    /** totalAmount = weightKg * pricePerKg */
    private BigDecimal totalAmount;

    /** pending / confirmed / paid / shipped / completed / cancelled */
    private String status;

    private String paymentMethod;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
