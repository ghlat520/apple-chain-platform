package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Dashboard (big-screen) config. Table: bd_dashboard_config
 */
@Getter
@Setter
@TableName("bd_dashboard_config")
public class BdDashboardConfig extends BaseEntity {

    private String dashboardCode;
    private String dashboardName;

    /** GOV / OPS / INDUSTRY / CUSTOM */
    private String category;

    private String layoutJson;
    private String theme;

    /** PUBLIC / PRIVATE / ROLE */
    private String visibility;

    private String visibleRoles;
    private Integer published;
    private String owner;
    private Integer refreshSeconds;
    private String remark;
}
