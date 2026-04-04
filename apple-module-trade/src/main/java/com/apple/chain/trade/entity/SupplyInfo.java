package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Supply listing from farmers.
 * Table: td_supply_info
 */
@Getter
@Setter
@TableName("td_supply_info")
public class SupplyInfo extends BaseEntity {

    /** Unique supply number, auto-generated */
    private String supplyNo;

    private Long farmerId;
    private Long orchardId;
    private String variety;

    /** Available quantity in kg */
    private BigDecimal quantity;

    /** Expected price per kg in yuan */
    private BigDecimal priceExpected;

    private LocalDate harvestDate;
    private LocalDate validUntil;

    /** Quality grade: A / B / C */
    private String quality;

    private String location;
    private String description;

    /** DRAFT / PUBLISHED / MATCHED / CLOSED */
    private String status;
}
