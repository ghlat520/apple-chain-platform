package com.apple.chain.trace.mapper;

import com.apple.chain.trace.entity.TraceBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * TraceBatch mapper.
 */
@Mapper
public interface TraceBatchMapper extends BaseMapper<TraceBatch> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(batch_code, 11) AS BIGINT)), 0) + 1 " +
            "FROM trace_batch WHERE batch_code LIKE CONCAT('TB', #{prefix}, '%')")
    int nextSeq(String prefix);

    @Select("SELECT * FROM trace_batch WHERE batch_code = #{batchCode} AND deleted = 0 LIMIT 1")
    TraceBatch findByBatchCode(String batchCode);
}
