package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdMetricDefinition;
import com.apple.chain.bigdata.entity.BdMetricValue;
import com.apple.chain.bigdata.mapper.BdMetricDefinitionMapper;
import com.apple.chain.bigdata.mapper.BdMetricValueMapper;
import com.apple.chain.bigdata.service.BdMetricService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BdMetricServiceImpl
        extends ServiceImpl<BdMetricDefinitionMapper, BdMetricDefinition>
        implements BdMetricService {

    private final BdMetricValueMapper valueMapper;

    @Override
    public List<BdMetricValue> listValues(String metricCode, LocalDate from, LocalDate to) {
        LambdaQueryWrapper<BdMetricValue> wrapper = new LambdaQueryWrapper<BdMetricValue>()
                .eq(BdMetricValue::getMetricCode, metricCode)
                .orderByAsc(BdMetricValue::getStatDate);
        if (from != null) {
            wrapper.ge(BdMetricValue::getStatDate, from);
        }
        if (to != null) {
            wrapper.le(BdMetricValue::getStatDate, to);
        }
        return valueMapper.selectList(wrapper);
    }

    @Override
    public BdMetricValue recordValue(BdMetricValue value) {
        valueMapper.insert(value);
        return value;
    }
}
