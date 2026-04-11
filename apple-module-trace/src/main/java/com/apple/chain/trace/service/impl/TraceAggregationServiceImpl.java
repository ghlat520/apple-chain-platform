package com.apple.chain.trace.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trace.dto.TraceFullChainVO;
import com.apple.chain.trace.entity.TraceChain;
import com.apple.chain.trace.entity.TraceNode;
import com.apple.chain.trace.mapper.TraceChainMapper;
import com.apple.chain.trace.mapper.TraceNodeMapper;
import com.apple.chain.trace.service.TraceAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TraceAggregationServiceImpl implements TraceAggregationService {

    private final TraceChainMapper traceChainMapper;
    private final TraceNodeMapper traceNodeMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public TraceFullChainVO getFullChain(String traceCode) {
        TraceChain chain = traceChainMapper.findByTraceCode(traceCode);
        if (chain == null) {
            throw new BizException(ResultCode.NOT_FOUND, "溯源码不存在: " + traceCode);
        }

        TraceFullChainVO vo = new TraceFullChainVO();
        vo.setTraceCode(chain.getTraceCode());
        vo.setBatchNo(chain.getBatchNo());
        vo.setCurrentStatus(chain.getCurrentStatus());
        vo.setChainTxHash(chain.getDataHash());
        vo.setChainStatus(chain.getChainStatus());

        // Timeline nodes
        List<TraceNode> nodes = traceNodeMapper.findByTraceCode(traceCode);
        vo.setTimeline(nodes);

        // Cross-module: orchard info
        if (chain.getOrchardId() != null) {
            try {
                List<Map<String, Object>> orchardRows = jdbcTemplate.queryForList(
                        "SELECT name, variety, location, area_mu FROM orchard WHERE id = ? AND deleted = 0",
                        chain.getOrchardId());
                if (!orchardRows.isEmpty()) {
                    Map<String, Object> orchard = orchardRows.get(0);
                    vo.setOrchardName((String) orchard.get("name"));
                    vo.setVariety((String) orchard.get("variety"));
                    vo.setRegion((String) orchard.get("location"));
                }
            } catch (Exception ignored) { /* table may not exist in test */ }
        }

        // Cross-module: farmer info
        if (chain.getFarmerId() != null) {
            try {
                List<Map<String, Object>> userRows = jdbcTemplate.queryForList(
                        "SELECT name, phone FROM sys_user WHERE id = ? AND deleted = 0",
                        chain.getFarmerId());
                if (!userRows.isEmpty()) {
                    vo.setFarmerName((String) userRows.get(0).get("name"));
                }
            } catch (Exception ignored) { }
        }

        // Cross-module: input materials (agri_usage by traceCode)
        try {
            List<Map<String, Object>> usages = jdbcTemplate.queryForList(
                    "SELECT product_name, method, quantity, unit, operation_date FROM agri_usage WHERE trace_code = ? AND deleted = 0",
                    traceCode);
            vo.setInputMaterials(usages);
        } catch (Exception ignored) { }

        // Cross-module: warehouse records
        try {
            List<Map<String, Object>> whRecords = jdbcTemplate.queryForList(
                    "SELECT warehouse_name, record_type, quantity, temperature, humidity, record_date FROM wh_warehouse_record WHERE trace_code = ? AND deleted = 0",
                    traceCode);
            vo.setWarehouseRecords(whRecords);
        } catch (Exception ignored) { }

        // Cross-module: trade info
        if (chain.getBatchNo() != null) {
            try {
                List<Map<String, Object>> tradeRows = jdbcTemplate.queryForList(
                        "SELECT order_no, variety, quantity, unit_price, total_amount, order_status, trade_date FROM td_trade_order WHERE batch_code = ? AND deleted = 0",
                        chain.getBatchNo());
                if (!tradeRows.isEmpty()) {
                    vo.setTradeInfo(tradeRows.get(0));
                }
            } catch (Exception ignored) { }
        }

        return vo;
    }
}
