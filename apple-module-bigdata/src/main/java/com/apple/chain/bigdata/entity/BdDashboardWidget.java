package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Dashboard widget. Table: bd_dashboard_widget
 */
@Getter
@Setter
@TableName("bd_dashboard_widget")
public class BdDashboardWidget extends BaseEntity {

    private Long dashboardId;
    private String widgetCode;
    private String widgetName;

    /** KPI / LINE / BAR / PIE / TABLE / MAP / TRACE / TEXT */
    private String widgetType;

    /** METRIC / SQL / API */
    private String dataSource;

    private String dataRef;
    private String positionJson;
    private String optionJson;
    private Integer ordinal;
}
