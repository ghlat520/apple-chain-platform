package com.apple.chain.trace.mapper;

import com.apple.chain.trace.entity.ChainRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChainRecordMapper extends BaseMapper<ChainRecord> {

    /** Most recent chain record for a trace_code (success preferred over pending). */
    @Select("SELECT * FROM tr_chain_record " +
            "WHERE deleted = 0 AND trace_code = #{traceCode} " +
            "ORDER BY chain_status DESC, update_time DESC LIMIT 1")
    ChainRecord findLatestByTraceCode(@Param("traceCode") String traceCode);

    /** Admin list filter by status. */
    @Select("SELECT * FROM tr_chain_record " +
            "WHERE deleted = 0 " +
            "  AND (#{status} IS NULL OR chain_status = #{status}) " +
            "ORDER BY id DESC LIMIT #{limit}")
    List<ChainRecord> findByStatus(@Param("status") Integer status, @Param("limit") int limit);
}
