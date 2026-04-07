package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdMetricDefinition;
import com.apple.chain.bigdata.entity.BdMetricValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

/** Metric center: definitions + value snapshots. */
public interface BdMetricService extends IService<BdMetricDefinition> {

    List<BdMetricValue> listValues(String metricCode, LocalDate from, LocalDate to);

    BdMetricValue recordValue(BdMetricValue value);
}
