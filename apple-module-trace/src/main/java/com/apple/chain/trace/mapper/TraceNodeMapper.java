package com.apple.chain.trace.mapper;

import com.apple.chain.trace.entity.TraceNode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Trace node mapper.
 */
@Mapper
public interface TraceNodeMapper extends BaseMapper<TraceNode> {

    @Select("SELECT * FROM tr_trace_node WHERE trace_code = #{traceCode} AND deleted = 0 ORDER BY node_time ASC")
    List<TraceNode> findByTraceCode(String traceCode);
}
