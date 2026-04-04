package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Orchard entity.
 * Table: pt_orchard
 */
@Getter
@Setter
@TableName("pt_orchard")
public class Orchard extends BaseEntity {

    /** Unique orchard number, format: ORD+yyyyMMdd+seq */
    private String orchardNo;

    private String orchardName;

    /** FK to uc_user.id */
    private Long farmerId;

    /** Area in mu */
    private BigDecimal area;

    /** Apple variety e.g. 红富士, 嘎拉, 黄元帅 */
    private String variety;

    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;

    /** Age of trees in years */
    private Integer treeAge;

    /** NORMAL / DORMANT / HARVESTED */
    private String status;

    private String remark;
}
