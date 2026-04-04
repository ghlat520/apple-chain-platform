package com.apple.chain.planting.mapper;

import com.apple.chain.planting.entity.CultivationBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * CultivationBatch mapper.
 */
@Mapper
public interface CultivationBatchMapper extends BaseMapper<CultivationBatch> {

    /**
     * Returns next sequence number for batch code generation.
     * Pattern: CB + yyyyMMdd + 4-digit seq.
     */
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(batch_code, 11) AS BIGINT)), 0) + 1 " +
            "FROM cultivation_batch WHERE batch_code LIKE CONCAT('CB', #{prefix}, '%')")
    int nextSeq(String prefix);
}
