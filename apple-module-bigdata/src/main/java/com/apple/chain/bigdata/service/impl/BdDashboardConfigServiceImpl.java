package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdDashboardConfig;
import com.apple.chain.bigdata.entity.BdDashboardWidget;
import com.apple.chain.bigdata.mapper.BdDashboardConfigMapper;
import com.apple.chain.bigdata.mapper.BdDashboardWidgetMapper;
import com.apple.chain.bigdata.service.BdDashboardConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BdDashboardConfigServiceImpl
        extends ServiceImpl<BdDashboardConfigMapper, BdDashboardConfig>
        implements BdDashboardConfigService {

    private final BdDashboardWidgetMapper widgetMapper;

    @Override
    public List<BdDashboardWidget> listWidgets(Long dashboardId) {
        return widgetMapper.selectList(
                new LambdaQueryWrapper<BdDashboardWidget>()
                        .eq(BdDashboardWidget::getDashboardId, dashboardId)
                        .orderByAsc(BdDashboardWidget::getOrdinal));
    }

    @Override
    public BdDashboardWidget addWidget(BdDashboardWidget widget) {
        widgetMapper.insert(widget);
        return widget;
    }

    @Override
    public boolean removeWidget(Long widgetId) {
        return widgetMapper.deleteById(widgetId) > 0;
    }

    @Override
    public boolean publish(Long dashboardId) {
        BdDashboardConfig cfg = getById(dashboardId);
        if (cfg == null) {
            return false;
        }
        cfg.setPublished(1);
        return updateById(cfg);
    }
}
