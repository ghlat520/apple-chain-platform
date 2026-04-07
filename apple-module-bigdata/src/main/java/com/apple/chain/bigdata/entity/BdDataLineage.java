package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Data lineage (upstream -> downstream). Table: bd_data_lineage
 */
@Getter
@Setter
@TableName("bd_data_lineage")
public class BdDataLineage extends BaseEntity {

    /** TABLE / FIELD / JOB / METRIC / DASHBOARD / SYSTEM */
    private String upstreamType;

    private String upstreamId;
    private String upstreamName;

    private String downstreamType;
    private String downstreamId;
    private String downstreamName;

    /** DERIVES_FROM / TRANSFORMS / AGGREGATES / REFERENCES */
    private String relationType;

    private Long pipelineJobId;
    private String description;
}
