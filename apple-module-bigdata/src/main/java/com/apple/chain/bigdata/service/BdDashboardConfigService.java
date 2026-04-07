package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdDashboardConfig;
import com.apple.chain.bigdata.entity.BdDashboardWidget;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/** Dashboard (big-screen) config + widgets. */
public interface BdDashboardConfigService extends IService<BdDashboardConfig> {

    List<BdDashboardWidget> listWidgets(Long dashboardId);

    BdDashboardWidget addWidget(BdDashboardWidget widget);

    boolean removeWidget(Long widgetId);

    boolean publish(Long dashboardId);
}
