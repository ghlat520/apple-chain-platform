package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Growth record entity for orchard field operations.
 * Table: pt_growth_record
 */
@Getter
@Setter
@TableName("pt_growth_record")
public class GrowthRecord extends BaseEntity {

    private Long orchardId;

    /**
     * Operation type: FERTILIZE / SPRAY / IRRIGATE / PRUNE / PEST_CONTROL
     */
    private String recordType;

    private LocalDate operateDate;
    private String operator;

    /**
     * JSON array: [{name, amount, unit}]
     * e.g. [{"name":"复合肥","amount":"50","unit":"kg"}]
     */
    private String materials;

    private String weather;
    private String notes;

    /**
     * JSON array string of uploaded photo URLs — produced by
     * apple-module-file upload endpoint, e.g. {@code ["/api/files/ab12","/api/files/cd34"]}.
     * Nullable; clients may send null or {@code "[]"} when no photos attached.
     */
    private String photoUrls;

    private Long recordedBy;
}
