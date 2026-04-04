package com.apple.chain.trace.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trace.entity.TraceChain;
import com.apple.chain.trace.entity.TraceNode;
import com.apple.chain.trace.mapper.TraceChainMapper;
import com.apple.chain.trace.mapper.TraceNodeMapper;
import com.apple.chain.trace.service.TraceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TraceServiceImpl extends ServiceImpl<TraceChainMapper, TraceChain> implements TraceService {

    private final TraceNodeMapper traceNodeMapper;

    @Override
    public IPage<TraceChain> listChains(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<TraceChain> wrapper = new LambdaQueryWrapper<TraceChain>()
                .like(StringUtils.hasText(keyword), TraceChain::getTraceCode, keyword)
                .or(StringUtils.hasText(keyword), w -> w.like(StringUtils.hasText(keyword), TraceChain::getBatchNo, keyword))
                .eq(StringUtils.hasText(status), TraceChain::getCurrentStatus, status)
                .orderByDesc(TraceChain::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> getTraceDetail(String traceCode) {
        TraceChain chain = baseMapper.findByTraceCode(traceCode);
        if (chain == null) {
            throw new BizException(ResultCode.NOT_FOUND, "溯源码不存在: " + traceCode);
        }
        List<TraceNode> nodes = traceNodeMapper.findByTraceCode(traceCode);

        Map<String, Object> result = new HashMap<>();
        result.put("chain", chain);
        result.put("nodes", nodes);
        return result;
    }

    @Override
    public Map<String, Object> publicScan(String traceCode) {
        TraceChain chain = baseMapper.findByTraceCode(traceCode);
        if (chain == null) {
            throw new BizException(ResultCode.NOT_FOUND, "未找到溯源信息，请核实溯源码");
        }
        List<TraceNode> nodes = traceNodeMapper.findByTraceCode(traceCode);

        Map<String, Object> result = new HashMap<>();
        result.put("traceCode", chain.getTraceCode());
        result.put("productType", chain.getProductType());
        result.put("batchNo", chain.getBatchNo());
        result.put("currentStatus", chain.getCurrentStatus());
        result.put("chainStatus", chain.getChainStatus());
        result.put("timeline", nodes);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TraceNode addNode(TraceNode node) {
        TraceChain chain = baseMapper.findByTraceCode(node.getTraceCode());
        if (chain == null) {
            throw new BizException(ResultCode.NOT_FOUND, "溯源链不存在: " + node.getTraceCode());
        }
        traceNodeMapper.insert(node);

        // Update chain status to reflect latest node type
        TraceChain update = new TraceChain();
        update.setId(chain.getId());
        update.setCurrentStatus(mapNodeTypeToChainStatus(node.getNodeType()));
        baseMapper.updateById(update);

        return node;
    }

    @Override
    public void exportChains(String keyword, String status, HttpServletResponse response) {
        LambdaQueryWrapper<TraceChain> wrapper = new LambdaQueryWrapper<TraceChain>()
                .like(StringUtils.hasText(keyword), TraceChain::getTraceCode, keyword)
                .eq(StringUtils.hasText(status), TraceChain::getCurrentStatus, status)
                .orderByDesc(TraceChain::getCreateTime);
        List<TraceChain> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("溯源链列表.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("溯源码,产品类型,批次号,果园ID,农户ID,当前状态,链上状态,创建时间");
            for (TraceChain c : list) {
                writer.println(
                        c.getTraceCode() + "," + c.getProductType() + "," + c.getBatchNo() + "," +
                        c.getOrchardId() + "," + c.getFarmerId() + "," +
                        c.getCurrentStatus() + "," +
                        (c.getChainStatus() == 1 ? "已上链" : "待上链") + "," +
                        c.getCreateTime()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    private String mapNodeTypeToChainStatus(String nodeType) {
        return switch (nodeType) {
            case "PLANT" -> "PLANTED";
            case "HARVEST" -> "HARVESTED";
            case "STORAGE" -> "IN_STORAGE";
            case "LOGISTICS" -> "IN_TRANSIT";
            case "TRADE" -> "SOLD";
            default -> "PLANTED";
        };
    }
}
