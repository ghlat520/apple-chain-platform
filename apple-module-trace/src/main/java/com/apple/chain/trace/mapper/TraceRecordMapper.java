package com.apple.chain.trace.mapper;

import com.apple.chain.trace.entity.TraceRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * TraceRecord mapper.
 */
@Mapper
public interface TraceRecordMapper extends BaseMapper<TraceRecord> {

    @Select("SELECT * FROM trace_record WHERE batch_id = #{batchId} ORDER BY record_time ASC, create_time ASC")
    List<TraceRecord> findByBatchId(Long batchId);
}
