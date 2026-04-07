package com.apple.chain.trace.mapper;

import com.apple.chain.trace.entity.TraceCode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * TraceCode mapper. M12 three-level code persistence.
 */
@Mapper
public interface TraceCodeMapper extends BaseMapper<TraceCode> {

    @Select("SELECT * FROM trace_code WHERE code = #{code} AND deleted = 0 LIMIT 1")
    TraceCode findByCode(@Param("code") String code);

    @Select("SELECT * FROM trace_code WHERE parent_code = #{parentCode} AND deleted = 0 ORDER BY id")
    List<TraceCode> findByParentCode(@Param("parentCode") String parentCode);

    @Select("SELECT COUNT(1) FROM trace_code WHERE parent_code = #{parentCode} AND granularity = #{granularity} AND deleted = 0")
    int countByParent(@Param("parentCode") String parentCode, @Param("granularity") String granularity);

    @Select("SELECT * FROM trace_code WHERE batch_id = #{batchId} AND granularity = #{granularity} AND deleted = 0 ORDER BY id")
    List<TraceCode> findByBatchAndGranularity(@Param("batchId") Long batchId, @Param("granularity") String granularity);
}
