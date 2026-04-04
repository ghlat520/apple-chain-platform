package com.apple.chain.trace.mapper;

import com.apple.chain.trace.entity.TraceChain;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Trace chain mapper.
 */
@Mapper
public interface TraceChainMapper extends BaseMapper<TraceChain> {

    @Select("SELECT * FROM tr_trace_chain WHERE trace_code = #{traceCode} AND deleted = 0 LIMIT 1")
    TraceChain findByTraceCode(String traceCode);
}
